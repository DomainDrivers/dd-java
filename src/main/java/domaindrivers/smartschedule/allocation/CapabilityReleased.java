package domaindrivers.smartschedule.allocation;


import domaindrivers.smartschedule.shared.PrivateEvent;

import java.time.Instant;
import java.util.UUID;

record CapabilityReleased(UUID eventId, ProjectAllocationsId projectId, Demands missingDemands, Instant occurredAt) implements PrivateEvent {

    public CapabilityReleased(ProjectAllocationsId projectId, Demands missingDemands, Instant occuredAt) {
        this(UUID.randomUUID(), projectId, missingDemands, occuredAt);
    }
}
