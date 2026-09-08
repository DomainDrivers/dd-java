package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;

record ProjectCreated(ProjectAllocationsId projectId, TimeSlot slot, Instant occurredAt) {
}
