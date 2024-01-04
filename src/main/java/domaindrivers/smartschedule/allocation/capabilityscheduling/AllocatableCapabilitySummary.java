package domaindrivers.smartschedule.allocation.capabilityscheduling;

import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

public record AllocatableCapabilitySummary(AllocatableCapabilityId id, AllocatableResourceId allocatableResourceId,
                                           Capability capability, TimeSlot timeSlot) {
}
