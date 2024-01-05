package domaindrivers.smartschedule.shared;

import java.time.Instant;

//metadata:
//correlationId
//potential aggregate's id
//causationId - id of a message that caused this message
//messageId - unique id of the
//user - if there is any (might be a system event)
public interface PublishedEvent {

    Instant occurredAt();
}
