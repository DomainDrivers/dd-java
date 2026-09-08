package domaindrivers.smartschedule.allocation;

import java.time.Instant;
import java.util.Optional;

record ReleaseOutcome(Demands missingDemandsAfter, Optional<Instant> occurredAt) {
}
