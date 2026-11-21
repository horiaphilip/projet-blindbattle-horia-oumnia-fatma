package fr.miage.blindbattle.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GameEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(GameEventListener.class);
    private final JsonMapper mapper;
    private final SseBroadcaster broadcaster;

    public GameEventListener(JsonMapper mapper, SseBroadcaster broadcaster) {
        this.mapper = mapper;
        this.broadcaster = broadcaster;
    }

    @JmsListener(destination = Destinations.PREFIX + Destinations.GAME_EVENTS, containerFactory = "topicListenerFactory")
    public void receive(String body) {
        GameEvent event = mapper.readValue(body, GameEvent.class);
        LOG.info("Reçu {} partie={} écrans={}", event.type(), event.gameCode(), broadcaster.subscribers(event.gameCode()));
        broadcaster.broadcast(event.gameCode(), event.eventId().toString(), event.type().name(), body);
    }
}
