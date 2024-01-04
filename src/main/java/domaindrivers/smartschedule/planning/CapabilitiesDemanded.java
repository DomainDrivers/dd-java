package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.shared.Event;

import java.time.Instant;
import java.util.UUID;

public record CapabilitiesDemanded(UUID uuid, ProjectId projectId, Demands demands, Instant occurredAt) implements Event {

    public CapabilitiesDemanded(ProjectId projectId, Demands demands, Instant occuredAt) {
        this(UUID.randomUUID(), projectId, demands, occuredAt);
    }

}
