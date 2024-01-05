package domaindrivers.smartschedule.allocation;


import java.time.Instant;
import java.util.UUID;

public record CapabilitiesAllocated(UUID eventId, UUID allocatedCapabilityId, ProjectAllocationsId projectId, Demands missingDemands, Instant occurredAt) {

    public CapabilitiesAllocated(UUID allocatedCapabilityId, ProjectAllocationsId projectId, Demands missingDemands, Instant occuredAt) {
        this(UUID.randomUUID(), allocatedCapabilityId, projectId, missingDemands, occuredAt);
    }
}
