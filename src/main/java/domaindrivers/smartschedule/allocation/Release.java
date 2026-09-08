package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;

record Release(AllocatableCapabilityId allocatableCapabilityId, TimeSlot slot, Instant when)
        implements ProjectAllocationsCommand {
}
