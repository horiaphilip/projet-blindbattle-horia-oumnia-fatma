package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.messaging.GameCommand;
import fr.miage.blindbattle.messaging.GameCommandType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RoundTimeoutHandler implements GameCommandHandler {
    private final JsonMapper mapper;
    private final RoundAggregator aggregator;

    public RoundTimeoutHandler(JsonMapper mapper, RoundAggregator aggregator) {
        this.mapper = mapper;
        this.aggregator = aggregator;
    }

    @Override
    public GameCommandType type() { return GameCommandType.ROUND_TIMEOUT; }

    @Override
    public void handle(String body) { aggregator.timeout(mapper.readValue(body, GameCommand.class).roundId()); }
}
