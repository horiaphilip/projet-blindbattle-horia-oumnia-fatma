package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.messaging.GameCommand;
import fr.miage.blindbattle.messaging.GameCommandType;
import fr.miage.blindbattle.service.RoundService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class NextRoundHandler implements GameCommandHandler {
    private final JsonMapper mapper;
    private final RoundService rounds;

    public NextRoundHandler(JsonMapper mapper, RoundService rounds) {
        this.mapper = mapper;
        this.rounds = rounds;
    }

    @Override
    public GameCommandType type() { return GameCommandType.NEXT_ROUND; }

    @Override
    public void handle(String body) { rounds.startNext(mapper.readValue(body, GameCommand.class).gameCode()); }
}
