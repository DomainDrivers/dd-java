package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.Event;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.UUID;

record ProjectAllocationScheduled(UUID uuid, ProjectAllocationsId projectId, TimeSlot fromTo,
                                         Instant occurredAt) implements Event {

    ProjectAllocationScheduled(ProjectAllocationsId projectId, TimeSlot fromTo, Instant occuredAt) {
        this(UUID.randomUUID(), projectId, fromTo, occuredAt);
    }

}
