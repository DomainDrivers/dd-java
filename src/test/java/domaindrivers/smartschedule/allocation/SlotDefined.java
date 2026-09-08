package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;

record SlotDefined(TimeSlot slot, Instant occurredAt) {
}
