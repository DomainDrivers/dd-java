package domaindrivers.smartschedule.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.UUID;

@Configuration
@EnableAsync
public class EventingConfiguration {

    @Bean
    EventsPublisher eventsPublisher(ApplicationEventPublisher applicationEventPublisher, OutboxRepository outboxRepository, ObjectMapper outboxObjectMapper) {
        return event -> {
            if (event instanceof OutboxEvent) {
                outboxRepository.save(toOutboxRecord(event, outboxObjectMapper));
            } else {
                applicationEventPublisher.publishEvent(event);
            }
        };
    }

    private OutboxRecord toOutboxRecord(PublishedEvent event, ObjectMapper outboxObjectMapper) {
        try {
            String payload = outboxObjectMapper.writeValueAsString(event);
            return new OutboxRecord(UUID.randomUUID(), event.getClass().getName(), payload, event.occurredAt());
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Bean
    ObjectMapper outboxObjectMapper() {
        return new JsonMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean
    OutboxRelay outboxRelay(OutboxRepository outboxRepository, ApplicationEventPublisher applicationEventPublisher, ObjectMapper outboxObjectMapper, Clock clock) {
        return new OutboxRelay(outboxRepository, applicationEventPublisher, outboxObjectMapper, clock);
    }
}
