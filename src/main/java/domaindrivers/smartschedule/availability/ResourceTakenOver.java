package domaindrivers.smartschedule.availability;

import domaindrivers.smartschedule.shared.Event;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ResourceTakenOver(UUID eventId, ResourceId resourceId, Set<Owner> previousOwners, TimeSlot slot, Instant occurredAt) implements Event {

    public ResourceTakenOver(ResourceId resourceId, Set<Owner> previousOwners, TimeSlot slot, Instant occuredAt) {
        this(UUID.randomUUID(), resourceId, previousOwners, slot, occuredAt);
    }
}
