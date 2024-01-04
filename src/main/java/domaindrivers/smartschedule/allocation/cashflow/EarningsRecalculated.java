package domaindrivers.smartschedule.allocation.cashflow;

import domaindrivers.smartschedule.allocation.ProjectAllocationsId;
import domaindrivers.smartschedule.shared.Event;

import java.time.Instant;
import java.util.UUID;

public record EarningsRecalculated(UUID uuid, ProjectAllocationsId projectId, Earnings earnings, Instant occurredAt) implements Event {

    public EarningsRecalculated(ProjectAllocationsId projectId, Earnings earnings, Instant instant) {
        this(UUID.randomUUID(), projectId, earnings, instant);
    }
}
