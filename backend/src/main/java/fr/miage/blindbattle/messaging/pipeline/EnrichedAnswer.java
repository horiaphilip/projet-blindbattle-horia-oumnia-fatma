package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.domain.GuessTarget;
import java.time.Instant;
import java.util.List;

public record EnrichedAnswer(SubmitAnswer answer, String expectedTitle, String expectedArtist, List<Long> choiceTrackIds,
        int correctChoiceIndex, Instant roundStartedAt, int roundSeconds, GuessTarget guessTarget, int currentStreak) {}
