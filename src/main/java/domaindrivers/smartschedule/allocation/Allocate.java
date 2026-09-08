package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.allocation.capabilityscheduling.AllocatableCapabilityId;
import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;

record Allocate(AllocatableCapabilityId allocatableCapabilityId, CapabilitySelector capability, TimeSlot requestedSlot,
                 Instant when) implements ProjectAllocationsCommand {
}
