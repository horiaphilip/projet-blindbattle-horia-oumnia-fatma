package fr.miage.blindbattle.messaging;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GameEventRelay {
    private final JmsTemplate topic;
    private final JsonMapper mapper;
    private final String destination;

    public GameEventRelay(@Qualifier("topicJmsTemplate") JmsTemplate topic, JsonMapper mapper,
            @Value("${blindbattle.jms.prefix:}") String prefix) {
        this.topic = topic;
        this.mapper = mapper;
        this.destination = prefix + Destinations.GAME_EVENTS;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void relay(GameEvent event) { topic.convertAndSend(destination, mapper.writeValueAsString(event)); }
}
