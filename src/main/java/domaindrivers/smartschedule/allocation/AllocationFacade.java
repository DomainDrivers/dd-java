package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitiesSummary;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilitySummary;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityFinder;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Owner;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


public class AllocationFacade {

    private final ProjectAllocationsRepository projectAllocationsRepository;
    private final AvailabilityFacade availabilityFacade;
    private final CapabilityFinder capabilityFinder;
    private final Clock clock;

    public AllocationFacade(ProjectAllocationsRepository projectAllocationsRepository, AvailabilityFacade availabilityFacade, CapabilityFinder capabilityFinder, Clock clock) {
        this.projectAllocationsRepository = projectAllocationsRepository;
        this.availabilityFacade = availabilityFacade;
        this.capabilityFinder = capabilityFinder;
        this.clock = clock;
    }

    @Transactional
    public ProjectAllocationsId createAllocation(TimeSlot timeSlot, Demands scheduledDemands) {
        ProjectAllocationsId projectId = ProjectAllocationsId.newOne();
        ProjectAllocations projectAllocations = new ProjectAllocations(projectId, Allocations.none(), scheduledDemands, timeSlot);
        projectAllocationsRepository.save(projectAllocations);
        return projectId;
    }

    public ProjectsAllocationsSummary findAllProjectsAllocations(Set<ProjectAllocationsId> projectIds) {
        return ProjectsAllocationsSummary.of(projectAllocationsRepository.findAllById(projectIds));
    }

    public ProjectsAllocationsSummary findAllProjectsAllocations() {
        return ProjectsAllocationsSummary.of(projectAllocationsRepository.findAll());
    }

    @Transactional
    public Optional<UUID> allocateToProject(ProjectAllocationsId projectId, AllocatableCapabilityId allocatableCapabilityId, Capability capability, TimeSlot timeSlot) {
        //yes, one transaction crossing 2 modules.
        if (!capabilityFinder.isPresent(allocatableCapabilityId)) {
            return Optional.empty();
        }
        if (!availabilityFacade.block(allocatableCapabilityId.toAvailabilityResourceId(), timeSlot, Owner.of(projectId.id()))) {
            return Optional.empty();
        }
        Optional<CapabilitiesAllocated> event = allocate(projectId, allocatableCapabilityId, capability, timeSlot);
        return event.map(CapabilitiesAllocated::allocatedCapabilityId);
    }

    private Optional<CapabilitiesAllocated> allocate(ProjectAllocationsId projectId, AllocatableCapabilityId allocatableCapabilityId, Capability capability, TimeSlot timeSlot) {
        ProjectAllocations allocations = projectAllocationsRepository.findById(projectId).orElseThrow();
        Optional<CapabilitiesAllocated> event = allocations.allocate(allocatableCapabilityId, capability, timeSlot, Instant.now(clock));
        projectAllocationsRepository.save(allocations);
        return event;
    }

    @Transactional
    public boolean releaseFromProject(ProjectAllocationsId projectId, AllocatableCapabilityId allocatableCapabilityId, TimeSlot timeSlot) {
        //can release not scheduled capability - at least for now. Hence no check to capabilityFinder
        availabilityFacade.release(allocatableCapabilityId.toAvailabilityResourceId(), timeSlot, Owner.of(projectId.id()));
        ProjectAllocations allocations = projectAllocationsRepository.findById(projectId).orElseThrow();
        Optional<CapabilityReleased> event = allocations.release(allocatableCapabilityId, timeSlot, Instant.now(clock));
        projectAllocationsRepository.save(allocations);
        return event.isPresent();
    }

    @Transactional
    boolean allocateCapabilityToProjectForPeriod(ProjectAllocationsId projectId, Capability capability, TimeSlot timeSlot) {
        AllocatableCapabilitiesSummary proposedCapabilities = capabilityFinder
                .findCapabilities(capability, timeSlot);
        if (proposedCapabilities.all().isEmpty()) {
            return false;
        }
        Set<ResourceId> availabilityResourceIds =
                proposedCapabilities.all()
                        .stream()
                        .map(resource -> resource.id().toAvailabilityResourceId())
                        .collect(Collectors.toSet());
        Optional<ResourceId> chosen = availabilityFacade.blockRandomAvailable(availabilityResourceIds, timeSlot, Owner.of(projectId.id()));
        if (chosen.isEmpty()) {
            return false;
        }
        AllocatableCapabilityId toAllocate = findChosenAllocatableCapability(proposedCapabilities, chosen.get());
        return allocate(projectId, toAllocate, capability, timeSlot).isPresent();
    }

    private AllocatableCapabilityId findChosenAllocatableCapability(AllocatableCapabilitiesSummary proposedCapabilities, ResourceId chosen) {
        return proposedCapabilities.all().stream()
                .map(AllocatableCapabilitySummary::id)
                .filter(id -> id.toAvailabilityResourceId().equals(chosen))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void editProjectDates(ProjectAllocationsId projectId, TimeSlot fromTo) {
        ProjectAllocations projectAllocations = projectAllocationsRepository.findById(projectId).orElseThrow();
        projectAllocations.defineSlot(fromTo, clock.instant());
    }

    @Transactional
    public void scheduleProjectAllocationDemands(ProjectAllocationsId projectId, Demands demands) {
        ProjectAllocations projectAllocations =
                projectAllocationsRepository.findById(projectId)
                        .orElseGet(() -> ProjectAllocations.empty(projectId));
        projectAllocations.addDemands(demands, Instant.now(clock));
        projectAllocationsRepository.save(projectAllocations);
    }
}

