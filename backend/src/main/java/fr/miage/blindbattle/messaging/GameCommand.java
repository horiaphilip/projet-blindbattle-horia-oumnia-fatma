package fr.miage.blindbattle.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameCommand(UUID commandId, GameCommandType type, String gameCode, Long roundId) {
    public static GameCommand roundTimeout(String gameCode, long roundId) {
        return new GameCommand(UUID.randomUUID(), GameCommandType.ROUND_TIMEOUT, gameCode, roundId);
    }

    public static GameCommand nextRound(String gameCode) {
        return new GameCommand(UUID.randomUUID(), GameCommandType.NEXT_ROUND, gameCode, null);
    }
}
