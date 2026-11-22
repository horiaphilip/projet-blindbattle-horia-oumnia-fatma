package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.domain.AnswerMode;
import fr.miage.blindbattle.messaging.GameCommandType;
import java.time.Instant;
import java.util.UUID;

public record SubmitAnswer(UUID eventId, GameCommandType type, String gameCode, long roundId, long playerId,
        AnswerMode mode, String text, Integer choiceIndex, Instant receivedAt) {
    public static SubmitAnswer of(UUID eventId, String gameCode, long roundId, long playerId, AnswerMode mode, String text,
            Integer choiceIndex, Instant receivedAt) {
        return new SubmitAnswer(eventId, GameCommandType.SUBMIT_ANSWER, gameCode, roundId, playerId, mode, text, choiceIndex,
                receivedAt);
    }
}
