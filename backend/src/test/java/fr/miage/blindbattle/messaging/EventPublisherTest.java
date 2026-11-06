package fr.miage.blindbattle.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventPublisherTest {
    private final JmsTemplate jms = mock(JmsTemplate.class);

    @Test void prefixesTheDestination() {
        new EventPublisher(jms, "g07.").publish(Destinations.USER_EVENTS, "{}");
        verify(jms).convertAndSend("g07.user.events", "{}");
    }

    @Test void keepsTheDestinationWithoutPrefix() {
        new EventPublisher(jms, "").publish(Destinations.USER_EVENTS, "{}");
        verify(jms).convertAndSend("user.events", "{}");
    }
}
