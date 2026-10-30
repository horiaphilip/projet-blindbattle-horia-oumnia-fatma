package fr.miage.blindbattle.messaging;

import fr.miage.blindbattle.service.OutboxService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelay {
    private final OutboxService outbox;

    public OutboxRelay(OutboxService outbox) { this.outbox = outbox; }

    @Scheduled(fixedDelayString = "${blindbattle.outbox.delay}")
    public void relay() { outbox.relayPending(); }
}
