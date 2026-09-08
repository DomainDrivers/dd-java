package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;

record DefineSlot(TimeSlot timeSlot, Instant when) implements ProjectAllocationsCommand {
}
