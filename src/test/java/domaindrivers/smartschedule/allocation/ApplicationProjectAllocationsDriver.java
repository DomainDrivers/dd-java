package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.MutableClock;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableResourceId;
import domaindrivers.smartschedule.allocation.capabilityscheduling.CapabilityScheduler;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.EventsPublisher;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class ApplicationProjectAllocationsDriver implements ProjectAllocationsDsl {

    private final AllocationFacade allocationFacade;
    private final CapabilityScheduler capabilityScheduler;
    private final EventsPublisher eventsPublisher;
    private final MutableClock clock;

    ApplicationProjectAllocationsDriver(AllocationFacade allocationFacade, CapabilityScheduler capabilityScheduler,
                                         EventsPublisher eventsPublisher, MutableClock clock) {
        this.allocationFacade = allocationFacade;
        this.capabilityScheduler = capabilityScheduler;
        this.eventsPublisher = eventsPublisher;
        this.clock = clock;
    }

    @Override
    public ProjectCreated createProject(TimeSlot slot, Demands demands) {
        clock.set(Instant.now());
        ProjectAllocationsId id = allocationFacade.createAllocation(slot, demands);
        ProjectAllocationScheduled published = lastPublishedFor(id).orElseThrow();
        return new ProjectCreated(id, published.fromTo(), published.occurredAt());
    }

    @Override
    public DemandsScheduled scheduleDemands(ProjectAllocationsId project, Demands demands, Instant when) {
        clock.set(when);
        allocationFacade.scheduleProjectAllocationDemands(project, demands);
        ProjectSnapshot snapshot = projectSnapshot(project);
        return new DemandsScheduled(snapshot.demands(), snapshot.missingDemands(), Optional.empty());
    }

    @Override
    public AllocatableCapabilityId scheduleAvailableCapability(CapabilitySelector capability, TimeSlot timeSlot) {
        List<AllocatableCapabilityId> ids = capabilityScheduler.scheduleResourceCapabilitiesForPeriod(
                AllocatableResourceId.newOne(), List.of(capability), timeSlot);
        return ids.get(0);
    }

    @Override
    public Optional<AllocationOutcome> allocate(ProjectAllocationsId project, AllocatableCapabilityId capabilityId,
                                                  CapabilitySelector capability, TimeSlot requestedSlot, Instant when) {
        clock.set(when);
        Optional<UUID> result = allocationFacade.allocateToProject(project, capabilityId, requestedSlot);
        return result.map(allocatedId -> new AllocationOutcome(allocatedId, projectSnapshot(project).missingDemands(), Optional.empty()));
    }

    @Override
    public Optional<ReleaseOutcome> release(ProjectAllocationsId project, AllocatableCapabilityId capabilityId,
                                             TimeSlot slot, Instant when) {
        clock.set(when);
        boolean released = allocationFacade.releaseFromProject(project, capabilityId, slot);
        if (!released) {
            return Optional.empty();
        }
        return Optional.of(new ReleaseOutcome(projectSnapshot(project).missingDemands(), Optional.empty()));
    }

    @Override
    public SlotDefined changeProjectSlot(ProjectAllocationsId project, TimeSlot slot, Instant when) {
        clock.set(when);
        allocationFacade.editProjectDates(project, slot);
        ProjectAllocationScheduled published = lastPublishedFor(project).orElseThrow();
        return new SlotDefined(published.fromTo(), published.occurredAt());
    }

    @Override
    public ProjectSnapshot projectSnapshot(ProjectAllocationsId project) {
        // findAllProjectsAllocations(Set<ProjectAllocationsId>) recurses infinitely against the real
        // JPA repository (JpaProjectAllocationsRepository.findAllById delegates to itself) - untouched
        // pre-existing production bug, routed around here rather than fixed (test code only this step).
        ProjectsAllocationsSummary summary = allocationFacade.findAllProjectsAllocations();
        Demands demands = summary.demands().get(project);
        Allocations allocations = summary.projectAllocations().get(project);
        TimeSlot timeSlot = summary.timeSlots().get(project);
        return new ProjectSnapshot(timeSlot != null, timeSlot, demands, demands.missingDemands(allocations), allocations);
    }

    private Optional<ProjectAllocationScheduled> lastPublishedFor(ProjectAllocationsId project) {
        ArgumentCaptor<ProjectAllocationScheduled> captor = ArgumentCaptor.forClass(ProjectAllocationScheduled.class);
        Mockito.verify(eventsPublisher, Mockito.atLeast(0)).publish(captor.capture());
        return captor.getAllValues().stream()
                .filter(event -> event.projectId().equals(project))
                .reduce((first, second) -> second);
    }
}
