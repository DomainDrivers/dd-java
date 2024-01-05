package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.shared.PublishedEvent;

import java.time.Instant;
import java.util.UUID;

public record CapabilitiesDemanded(UUID uuid, ProjectId projectId, Demands demands, Instant occurredAt) implements PublishedEvent {

    public CapabilitiesDemanded(ProjectId projectId, Demands demands, Instant occuredAt) {
        this(UUID.randomUUID(), projectId, demands, occuredAt);
    }

}
