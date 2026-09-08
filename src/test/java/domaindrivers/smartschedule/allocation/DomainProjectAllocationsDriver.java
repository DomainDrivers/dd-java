package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class DomainProjectAllocationsDriver implements ProjectAllocationsDsl {

    private final Map<ProjectAllocationsId, ProjectAllocations> projects = new HashMap<>();

    @Override
    public ProjectCreated createProject(TimeSlot slot, Demands demands) {
        ProjectAllocationsId id = ProjectAllocationsId.newOne();
        Instant when = Instant.now();
        projects.put(id, new ProjectAllocations(id, Allocations.none(), demands, slot));
        return new ProjectCreated(id, slot, when);
    }

    @Override
    public DemandsScheduled scheduleDemands(ProjectAllocationsId project, Demands demands, Instant when) {
        ProjectAllocations current = projects.computeIfAbsent(project,
                id -> demands.all().isEmpty() ? ProjectAllocations.empty(id) : ProjectAllocations.withDemands(id, Demands.none()));
        ProjectAllocationsDemandsScheduled event = current.addDemands(demands, when).orElseThrow();
        return new DemandsScheduled(current.demands(), current.missingDemands(), Optional.of(event.occurredAt()));
    }

    @Override
    public AllocatableCapabilityId scheduleAvailableCapability(CapabilitySelector capability, TimeSlot timeSlot) {
        return AllocatableCapabilityId.newOne();
    }

    @Override
    public Optional<AllocationOutcome> allocate(ProjectAllocationsId project, AllocatableCapabilityId capabilityId,
                                                  CapabilitySelector capability, TimeSlot requestedSlot, Instant when) {
        ProjectAllocations current = projects.get(project);
        return current.allocate(capabilityId, capability, requestedSlot, when)
                .map(event -> new AllocationOutcome(event.allocatedCapabilityId(), event.missingDemands(), Optional.of(event.occurredAt())));
    }

    @Override
    public Optional<ReleaseOutcome> release(ProjectAllocationsId project, AllocatableCapabilityId capabilityId,
                                             TimeSlot slot, Instant when) {
        ProjectAllocations current = projects.get(project);
        return current.release(capabilityId, slot, when)
                .map(event -> new ReleaseOutcome(event.missingDemands(), Optional.of(event.occurredAt())));
    }

    @Override
    public SlotDefined changeProjectSlot(ProjectAllocationsId project, TimeSlot slot, Instant when) {
        ProjectAllocations current = projects.get(project);
        ProjectAllocationScheduled event = current.defineSlot(slot, when).orElseThrow();
        return new SlotDefined(event.fromTo(), event.occurredAt());
    }

    @Override
    public ProjectSnapshot projectSnapshot(ProjectAllocationsId project) {
        ProjectAllocations current = projects.get(project);
        return new ProjectSnapshot(current.hasTimeSlot(), current.timeSlot(), current.demands(), current.missingDemands(), current.allocations());
    }
}
