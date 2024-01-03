package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public class AllocationFacade {

    private final ProjectAllocationsRepository projectAllocationsRepository;
    private final Clock clock;

    public AllocationFacade(ProjectAllocationsRepository projectAllocationsRepository, Clock clock) {
        this.projectAllocationsRepository = projectAllocationsRepository;
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
    public Optional<UUID> allocateToProject(ProjectAllocationsId projectId, ResourceId resourceId, Capability capability, TimeSlot timeSlot) {
        ProjectAllocations allocations = projectAllocationsRepository.findById(projectId).orElseThrow();
        Optional<CapabilitiesAllocated> event = allocations.allocate(resourceId, capability, timeSlot, Instant.now(clock));
        projectAllocationsRepository.save(allocations);
        return event.map(CapabilitiesAllocated::allocatedCapabilityId);

    }

    @Transactional
    public boolean releaseFromProject(ProjectAllocationsId projectId, UUID allocatableCapabilityId, TimeSlot timeSlot) {
        ProjectAllocations allocations = projectAllocationsRepository.findById(projectId).orElseThrow();
        Optional<CapabilityReleased> event = allocations.release(allocatableCapabilityId, timeSlot, Instant.now(clock));
        projectAllocationsRepository.save(allocations);
        return event.isPresent();
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

