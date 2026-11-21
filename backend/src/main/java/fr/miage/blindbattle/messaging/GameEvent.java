package fr.miage.blindbattle.messaging;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record GameEvent(UUID eventId, GameEventType type, String gameCode, Instant occurredAt, Map<String, Object> data) {
    public static GameEvent of(GameEventType type, String gameCode, Map<String, Object> data) {
        return new GameEvent(UUID.randomUUID(), type, gameCode, Instant.now(), data);
    }
}
