package fr.miage.blindbattle.dto;

import java.time.Instant;
import java.util.UUID;

public record OutboxEventView(long id, UUID eventId, String destination, String type, String status, Instant createdAt, Instant sentAt) {}
