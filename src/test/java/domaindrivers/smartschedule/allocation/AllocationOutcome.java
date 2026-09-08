package domaindrivers.smartschedule.allocation;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

record AllocationOutcome(UUID allocatedCapabilityId, Demands missingDemandsAfter, Optional<Instant> occurredAt) {
}
