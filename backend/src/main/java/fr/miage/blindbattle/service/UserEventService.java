package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.AuditLogEntity;
import fr.miage.blindbattle.domain.BadgeEntity;
import fr.miage.blindbattle.domain.ProcessedMessageEntity;
import fr.miage.blindbattle.domain.UserEntity;
import fr.miage.blindbattle.domain.UserStatsEntity;
import fr.miage.blindbattle.messaging.UserEvent;
import fr.miage.blindbattle.repository.AuditLogRepository;
import fr.miage.blindbattle.repository.BadgeRepository;
import fr.miage.blindbattle.repository.ProcessedMessageRepository;
import fr.miage.blindbattle.repository.UserRepository;
import fr.miage.blindbattle.repository.UserStatsRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserEventService {
    public static final String WELCOME_BADGE = "WELCOME";
    private static final Logger LOG = LoggerFactory.getLogger(UserEventService.class);
    private final UserRepository users;
    private final UserStatsRepository stats;
    private final BadgeRepository badges;
    private final AuditLogRepository audit;
    private final ProcessedMessageRepository processed;

    public UserEventService(UserRepository users, UserStatsRepository stats, BadgeRepository badges,
                            AuditLogRepository audit, ProcessedMessageRepository processed) {
        this.users = users;
        this.stats = stats;
        this.badges = badges;
        this.audit = audit;
        this.processed = processed;
    }

    @Transactional
    public void handle(UserEvent event) {
        if (processed.existsById(event.eventId())) {
            LOG.info("Événement {} déjà traité, ignoré", event.eventId());
            return;
        }
        switch (event.type()) {
            case USER_CREATED -> onCreated(event);
            case USER_DELETED -> audit.save(new AuditLogEntity("USER_DELETED", event.userId(),
                    "Compte de " + event.name() + " (" + event.email() + ") supprimé"));
        }
        processed.save(new ProcessedMessageEntity(event.eventId()));
    }

    private void onCreated(UserEvent event) {
        Optional<UserEntity> user = users.findById(event.userId());
        if (user.isEmpty()) {
            audit.save(new AuditLogEntity("USER_CREATED_IGNORED", event.userId(), "Utilisateur supprimé avant le traitement"));
            return;
        }
        stats.save(new UserStatsEntity(user.get()));
        badges.save(new BadgeEntity(user.get(), WELCOME_BADGE));
        audit.save(new AuditLogEntity("USER_CREATED", event.userId(),
                "Profil de statistiques et badge de bienvenue créés pour " + event.name()));
        LOG.info("Profil de statistiques créé pour userId={}", event.userId());
    }
}
