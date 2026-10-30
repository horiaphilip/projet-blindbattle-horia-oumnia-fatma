package fr.miage.blindbattle.dto;

import java.time.Instant;

public record AuditLogView(long id, String type, Long userId, String details, Instant createdAt) {}
