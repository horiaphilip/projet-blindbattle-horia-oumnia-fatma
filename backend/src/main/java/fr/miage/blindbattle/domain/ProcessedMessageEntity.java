package fr.miage.blindbattle.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_messages")
public class ProcessedMessageEntity {
    @Id
    private UUID eventId;
    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedMessageEntity() {}

    public ProcessedMessageEntity(UUID eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }

    public UUID getEventId() { return eventId; }
    public Instant getProcessedAt() { return processedAt; }
}
