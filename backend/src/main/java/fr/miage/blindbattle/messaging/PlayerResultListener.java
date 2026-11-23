package fr.miage.blindbattle.messaging;

import fr.miage.blindbattle.service.GameResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PlayerResultListener {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerResultListener.class);
    private final JsonMapper mapper;
    private final GameResultService results;

    public PlayerResultListener(JsonMapper mapper, GameResultService results) {
        this.mapper = mapper;
        this.results = results;
    }

    @JmsListener(destination = Destinations.PREFIX + Destinations.PLAYER_RESULTS)
    public void receive(String body) {
        PlayerResult result = mapper.readValue(body, PlayerResult.class);
        LOG.info("Reçu PLAYER_RESULT partie={} joueur={} rang={}", result.gameCode(), result.nickname(), result.rank());
        results.record(result);
    }
}
