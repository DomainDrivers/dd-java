package domaindrivers.smartschedule.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "outbox")
class OutboxRecord {

    @Id
    private UUID id;

    private String type;

    @Column(columnDefinition = "text")
    private String payload;

    private Instant occurredAt;

    private Instant sentAt;

    OutboxRecord() {
    }

    OutboxRecord(UUID id, String type, String payload, Instant occurredAt) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }

    UUID id() {
        return id;
    }

    String type() {
        return type;
    }

    String payload() {
        return payload;
    }

    void markSent(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
