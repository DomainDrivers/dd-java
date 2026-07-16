package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.availability.Owner;
import domaindrivers.smartschedule.availability.ResourceId;
import domaindrivers.smartschedule.shared.OutboxEvent;
import domaindrivers.smartschedule.shared.PublishedEvent;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.UUID;

public record ReleaseResource(UUID eventId, ResourceId resourceId, TimeSlot timeSlot, Owner owner,
                               Instant occurredAt) implements PublishedEvent, OutboxEvent {

    public ReleaseResource(ResourceId resourceId, TimeSlot timeSlot, Owner owner, Instant occurredAt) {
        this(UUID.randomUUID(), resourceId, timeSlot, owner, occurredAt);
    }
}
