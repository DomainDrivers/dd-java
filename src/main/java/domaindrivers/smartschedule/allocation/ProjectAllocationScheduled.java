package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.PrivateEvent;
import domaindrivers.smartschedule.shared.PublishedEvent;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.UUID;

public record ProjectAllocationScheduled(UUID uuid, ProjectAllocationsId projectId, TimeSlot fromTo,
                                         Instant occurredAt) implements PublishedEvent, PrivateEvent {

    public ProjectAllocationScheduled(ProjectAllocationsId projectId, TimeSlot fromTo, Instant occuredAt) {
        this(UUID.randomUUID(), projectId, fromTo, occuredAt);
    }

}
