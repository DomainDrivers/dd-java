package domaindrivers.smartschedule.allocation;


import domaindrivers.smartschedule.shared.PrivateEvent;

import java.time.Instant;
import java.util.UUID;

record CapabilitiesAllocated(UUID eventId, UUID allocatedCapabilityId, ProjectAllocationsId projectId, Demands missingDemands, Instant occurredAt) implements PrivateEvent {

    public CapabilitiesAllocated(UUID allocatedCapabilityId, ProjectAllocationsId projectId, Demands missingDemands, Instant occuredAt) {
        this(UUID.randomUUID(), allocatedCapabilityId, projectId, missingDemands, occuredAt);
    }
}
