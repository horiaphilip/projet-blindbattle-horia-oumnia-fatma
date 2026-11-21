package fr.miage.blindbattle.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class GameCommandSender {
    private final JmsTemplate jms;
    private final JsonMapper mapper;
    private final String destination;

    public GameCommandSender(JmsTemplate jms, JsonMapper mapper, @Value("${blindbattle.jms.prefix:}") String prefix) {
        this.jms = jms;
        this.mapper = mapper;
        this.destination = prefix + Destinations.GAME_COMMANDS;
    }

    public void send(String gameCode, Object command) {
        jms.convertAndSend(destination, mapper.writeValueAsString(command), message -> {
            message.setStringProperty("JMSXGroupID", gameCode);
            return message;
        });
    }
}
