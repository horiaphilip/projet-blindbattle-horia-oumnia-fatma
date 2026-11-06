package fr.miage.blindbattle.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {
    private final JmsTemplate jms;
    private final String prefix;

    public EventPublisher(JmsTemplate jms, @Value("${blindbattle.jms.prefix:}") String prefix) {
        this.jms = jms;
        this.prefix = prefix;
    }

    public void publish(String destination, String payload) { jms.convertAndSend(prefix + destination, payload); }
}
