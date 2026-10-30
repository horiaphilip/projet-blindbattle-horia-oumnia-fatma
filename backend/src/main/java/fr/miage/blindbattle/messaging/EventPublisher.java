package fr.miage.blindbattle.messaging;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {
    private final JmsTemplate jms;

    public EventPublisher(JmsTemplate jms) { this.jms = jms; }

    public void publish(String destination, String payload) { jms.convertAndSend(destination, payload); }
}
