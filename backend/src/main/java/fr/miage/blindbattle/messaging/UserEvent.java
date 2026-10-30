package fr.miage.blindbattle.messaging;

import fr.miage.blindbattle.domain.UserEntity;
import java.time.Instant;
import java.util.UUID;

public record UserEvent(UUID eventId, UserEventType type, long userId, String name, String email, Instant occurredAt) {
    public static UserEvent created(UserEntity user) { return of(UserEventType.USER_CREATED, user); }

    public static UserEvent deleted(UserEntity user) { return of(UserEventType.USER_DELETED, user); }

    private static UserEvent of(UserEventType type, UserEntity user) {
        return new UserEvent(UUID.randomUUID(), type, user.getId(), user.getName(), user.getEmail(), Instant.now());
    }
}
