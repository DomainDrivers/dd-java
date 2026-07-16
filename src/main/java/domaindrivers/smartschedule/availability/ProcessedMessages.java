package domaindrivers.smartschedule.availability;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class ProcessedMessages {

    private final Set<UUID> claimedIds = ConcurrentHashMap.newKeySet();

    boolean claim(UUID messageId) {
        return claimedIds.add(messageId);
    }
}
