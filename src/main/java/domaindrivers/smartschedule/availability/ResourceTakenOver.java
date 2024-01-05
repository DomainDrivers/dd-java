package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.PrivateEvent;
import domaindrivers.smartschedule.shared.PublishedEvent;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ResourceTakenOver(UUID eventId, ResourceId resourceId, Set<Owner> previousOwners, TimeSlot slot, Instant occurredAt) implements PublishedEvent {

    public ResourceTakenOver(ResourceId resourceId, Set<Owner> previousOwners, TimeSlot slot, Instant occuredAt) {
        this(UUID.randomUUID(), resourceId, previousOwners, slot, occuredAt);
    }
}
