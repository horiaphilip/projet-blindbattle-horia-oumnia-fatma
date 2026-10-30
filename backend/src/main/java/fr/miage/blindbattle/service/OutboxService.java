package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.OutboxEventEntity;
import fr.miage.blindbattle.domain.OutboxStatus;
import fr.miage.blindbattle.dto.OutboxEventView;
import fr.miage.blindbattle.messaging.EventPublisher;
import fr.miage.blindbattle.repository.OutboxEventRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class OutboxService {
    private static final Logger LOG = LoggerFactory.getLogger(OutboxService.class);
    private final OutboxEventRepository repository;
    private final EventPublisher publisher;
    private final JsonMapper mapper;

    public OutboxService(OutboxEventRepository repository, EventPublisher publisher, JsonMapper mapper) {
        this.repository = repository;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @Transactional
    public void record(String destination, UUID eventId, String type, Object event) {
        repository.save(new OutboxEventEntity(eventId, destination, type, mapper.writeValueAsString(event)));
    }

    @Transactional
    public void relayPending() {
        for (OutboxEventEntity event : repository.findTop50ByStatusOrderByIdAsc(OutboxStatus.PENDING)) {
            try {
                publisher.publish(event.getDestination(), event.getPayload());
            } catch (JmsException error) {
                LOG.warn("Broker indisponible, l'événement {} reste en attente : {}", event.getEventId(), error.getMessage());
                return;
            }
            event.markSent();
            LOG.info("Publié {} eventId={} sur {}", event.getType(), event.getEventId(), event.getDestination());
        }
    }

    @Transactional(readOnly = true)
    public List<OutboxEventView> list() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(e -> new OutboxEventView(e.getId(), e.getEventId(), e.getDestination(), e.getType(),
                        e.getStatus().name(), e.getCreatedAt(), e.getSentAt()))
                .toList();
    }
}
