package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

record Demand(Capability capability, TimeSlot slot) {
}
