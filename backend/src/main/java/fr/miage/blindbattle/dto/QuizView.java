package fr.miage.blindbattle.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizView(long id, String title, String description, String visibility, UUID shareToken, long ownerId,
        String ownerName, int trackCount, int playableTrackCount, Instant createdAt, Instant updatedAt,
        List<QuizTrackView> tracks) {}
