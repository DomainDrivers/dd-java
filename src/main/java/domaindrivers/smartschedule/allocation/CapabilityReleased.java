package domaindrivers.smartschedule.allocation;


import java.time.Instant;
import java.util.UUID;

public record CapabilityReleased(UUID eventId, ProjectAllocationsId projectId, Demands missingDemands, Instant occurredAt) {

    public CapabilityReleased(ProjectAllocationsId projectId, Demands missingDemands, Instant occuredAt) {
        this(UUID.randomUUID(), projectId, missingDemands, occuredAt);
    }
}
