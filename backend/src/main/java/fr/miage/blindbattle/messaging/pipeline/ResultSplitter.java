package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.messaging.Destinations;
import fr.miage.blindbattle.messaging.EventPublisher;
import fr.miage.blindbattle.messaging.GameEvent;
import fr.miage.blindbattle.messaging.GameEventType;
import fr.miage.blindbattle.messaging.PlayerResult;
import fr.miage.blindbattle.service.GameResultService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ResultSplitter {
    private static final Logger LOG = LoggerFactory.getLogger(ResultSplitter.class);
    private final JsonMapper mapper;
    private final GameResultService results;
    private final EventPublisher publisher;

    public ResultSplitter(JsonMapper mapper, GameResultService results, EventPublisher publisher) {
        this.mapper = mapper;
        this.results = results;
        this.publisher = publisher;
    }

    @JmsListener(destination = Destinations.PREFIX + Destinations.GAME_EVENTS, containerFactory = "topicListenerFactory")
    public void split(String body) {
        GameEvent event = mapper.readValue(body, GameEvent.class);
        if (event.type() != GameEventType.GAME_FINISHED) return;
        List<PlayerResult> split = results.split(event.gameCode(), event.eventId());
        split.forEach(result -> publisher.publish(Destinations.PLAYER_RESULTS, mapper.writeValueAsString(result)));
        LOG.info("Partie {} découpée en {} résultats individuels", event.gameCode(), split.size());
    }
}
