package domaindrivers.smartschedule.allocation;

import java.time.Instant;
import java.util.Optional;

record DemandsScheduled(Demands demandsAfter, Demands missingDemandsAfter, Optional<Instant> occurredAt) {
}
