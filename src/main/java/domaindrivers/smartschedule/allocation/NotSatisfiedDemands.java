package domaindrivers.smartschedule.allocation;

import domaindrivers.smartschedule.shared.PublishedEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotSatisfiedDemands(UUID uuid, Map<ProjectAllocationsId, Demands> missingDemands, Instant occurredAt) implements PublishedEvent {

    public NotSatisfiedDemands(Map<ProjectAllocationsId, Demands> missingDemands, Instant occuredAt) {
        this(UUID.randomUUID(), missingDemands, occuredAt);
    }

    public static NotSatisfiedDemands forOneProject(ProjectAllocationsId projectId, Demands scheduledDemands, Instant occurredAt) {
        return new NotSatisfiedDemands(UUID.randomUUID(), Map.of(projectId, scheduledDemands), occurredAt);
    }

    public static NotSatisfiedDemands allSatisfied(ProjectAllocationsId projectId, Instant occurredAt) {
        return new NotSatisfiedDemands(UUID.randomUUID(), Map.of(projectId, Demands.none()), occurredAt);
    }

}
