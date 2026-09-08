package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.Optional;

interface ProjectAllocationsDsl {

    ProjectCreated createProject(TimeSlot slot, Demands demands);

    DemandsScheduled scheduleDemands(ProjectAllocationsId project, Demands demands, Instant when);

    AllocatableCapabilityId scheduleAvailableCapability(CapabilitySelector capability, TimeSlot timeSlot);

    Optional<AllocationOutcome> allocate(ProjectAllocationsId project, AllocatableCapabilityId capabilityId,
                                          CapabilitySelector capability, TimeSlot requestedSlot, Instant when);

    Optional<ReleaseOutcome> release(ProjectAllocationsId project, AllocatableCapabilityId capabilityId,
                                      TimeSlot slot, Instant when);

    SlotDefined changeProjectSlot(ProjectAllocationsId project, TimeSlot slot, Instant when);

    ProjectSnapshot projectSnapshot(ProjectAllocationsId project);
}
