package domaindrivers.smartschedule.allocation.capabilityscheduling;

import domaindrivers.smartschedule.shared.CapabilitySelector;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

public record AllocatableCapabilitySummary(AllocatableCapabilityId id, AllocatableResourceId allocatableResourceId,
                                           CapabilitySelector capabilities, TimeSlot timeSlot) {
}
