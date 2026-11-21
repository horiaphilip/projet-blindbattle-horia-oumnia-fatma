package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.messaging.GameCommand;
import fr.miage.blindbattle.messaging.GameCommandType;
import fr.miage.blindbattle.service.RoundService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RoundTimeoutHandler implements GameCommandHandler {
    private final JsonMapper mapper;
    private final RoundService rounds;

    public RoundTimeoutHandler(JsonMapper mapper, RoundService rounds) {
        this.mapper = mapper;
        this.rounds = rounds;
    }

    @Override
    public GameCommandType type() { return GameCommandType.ROUND_TIMEOUT; }

    @Override
    public void handle(String body) { rounds.end(mapper.readValue(body, GameCommand.class).roundId()); }
}
