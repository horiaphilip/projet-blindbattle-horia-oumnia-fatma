package fr.miage.blindbattle.messaging;

import fr.miage.blindbattle.service.UserEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class UserEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(UserEventListener.class);
    private final JsonMapper mapper;
    private final UserEventService service;

    public UserEventListener(JsonMapper mapper, UserEventService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @JmsListener(destination = Destinations.USER_EVENTS)
    public void receive(String body) {
        UserEvent event = mapper.readValue(body, UserEvent.class);
        LOG.info("Reçu {} eventId={} userId={}", event.type(), event.eventId(), event.userId());
        service.handle(event);
    }
}
