package domaindrivers.smartschedule.allocation;


import domaindrivers.smartschedule.shared.Event;

import java.time.Instant;
import java.util.UUID;

public record ProjectAllocationsDemandsScheduled(UUID uuid, ProjectAllocationsId projectId, Demands missingDemands, Instant occurredAt) implements Event {

    public ProjectAllocationsDemandsScheduled(ProjectAllocationsId projectId, Demands missingDemands, Instant occuredAt) {
        this(UUID.randomUUID(), projectId, missingDemands, occuredAt);
    }

}
