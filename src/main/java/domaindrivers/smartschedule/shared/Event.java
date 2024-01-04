package domaindrivers.smartschedule.shared;

import java.time.Instant;

public interface Event {

    Instant occurredAt();
}
