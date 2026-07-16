package domaindrivers.smartschedule.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Instant;

class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper outboxObjectMapper;
    private final Clock clock;

    OutboxRelay(OutboxRepository outboxRepository, ApplicationEventPublisher applicationEventPublisher, ObjectMapper outboxObjectMapper, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.outboxObjectMapper = outboxObjectMapper;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 1000)
    void relayPendingMessages() {
        for (OutboxRecord record : outboxRepository.findBySentAtIsNull()) {
            applicationEventPublisher.publishEvent(deserialize(record));
            record.markSent(Instant.now(clock));
            outboxRepository.save(record);
        }
    }

    private Object deserialize(OutboxRecord record) {
        try {
            Class<?> type = Class.forName(record.type());
            return outboxObjectMapper.readValue(record.payload(), type);
        } catch (ClassNotFoundException | JsonProcessingException e) {
            throw new IllegalStateException("Cannot deserialize outbox record " + record.id(), e);
        }
    }
}
