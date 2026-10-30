package fr.miage.blindbattle;

import fr.miage.blindbattle.domain.OutboxStatus;
import fr.miage.blindbattle.dto.UserRequest;
import fr.miage.blindbattle.dto.UserView;
import fr.miage.blindbattle.messaging.UserEvent;
import fr.miage.blindbattle.messaging.UserEventType;
import fr.miage.blindbattle.repository.AuditLogRepository;
import fr.miage.blindbattle.repository.BadgeRepository;
import fr.miage.blindbattle.repository.OutboxEventRepository;
import fr.miage.blindbattle.service.StatsService;
import fr.miage.blindbattle.service.UserEventService;
import fr.miage.blindbattle.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:events", "spring.jpa.hibernate.ddl-auto=create-drop",
        "blindbattle.outbox.delay=200"})
class UserEventsTest {
    @Autowired UserService users;
    @Autowired StatsService stats;
    @Autowired UserEventService events;
    @Autowired OutboxEventRepository outbox;
    @Autowired BadgeRepository badges;
    @Autowired AuditLogRepository audit;

    @Test void userCreatedIsPublishedAndConsumed() {
        UserView user = users.create(new UserRequest("Ana", "ana.events@example.com"));
        await().atMost(Duration.ofSeconds(10)).ignoreExceptions().untilAsserted(() -> {
            assertThat(stats.forUser(user.id()).badges()).extracting("code").containsExactly(UserEventService.WELCOME_BADGE);
            assertThat(outbox.findTop50ByStatusOrderByIdAsc(OutboxStatus.PENDING)).isEmpty();
        });
    }

    @Test void duplicateEventIsProcessedOnce() {
        UserView user = users.create(new UserRequest("Lea", "lea.events@example.com"));
        await().atMost(Duration.ofSeconds(10)).until(() -> badges.findByUserIdOrderByEarnedAtAsc(user.id()).size() == 1);
        UUID eventId = outbox.findAll().stream()
                .filter(e -> e.getType().equals("USER_CREATED") && e.getPayload().contains("lea.events@example.com"))
                .findFirst().orElseThrow().getEventId();
        UserEvent replay = new UserEvent(eventId, UserEventType.USER_CREATED, user.id(), "Lea", "lea.events@example.com", Instant.now());
        long auditBefore = audit.count();
        events.handle(replay);
        events.handle(replay);
        assertThat(audit.count()).isEqualTo(auditBefore);
        assertThat(badges.findByUserIdOrderByEarnedAtAsc(user.id())).hasSize(1);
    }
}
