package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import domaindrivers.smartschedule.shared.capability.Capability;

import java.util.UUID;

record AllocatedCapability(UUID resourceId, Capability capability, TimeSlot timeSlot) {
}
