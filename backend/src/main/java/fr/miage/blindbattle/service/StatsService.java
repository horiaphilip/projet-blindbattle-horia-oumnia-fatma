package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.UserStatsEntity;
import fr.miage.blindbattle.dto.BadgeView;
import fr.miage.blindbattle.dto.StatsView;
import fr.miage.blindbattle.repository.BadgeRepository;
import fr.miage.blindbattle.repository.UserRepository;
import fr.miage.blindbattle.repository.UserStatsRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {
    private final UserRepository users;
    private final UserStatsRepository stats;
    private final BadgeRepository badges;

    public StatsService(UserRepository users, UserStatsRepository stats, BadgeRepository badges) {
        this.users = users;
        this.stats = stats;
        this.badges = badges;
    }

    @Transactional(readOnly = true)
    public StatsView forUser(long userId) {
        if (!users.existsById(userId)) throw new UserNotFound(userId);
        UserStatsEntity entity = stats.findByUserId(userId).orElseThrow(() -> new StatsNotReady(userId));
        List<BadgeView> badgeViews = badges.findByUserIdOrderByEarnedAtAsc(userId).stream()
                .map(b -> new BadgeView(b.getCode(), b.getEarnedAt()))
                .toList();
        return new StatsView(userId, entity.getGamesPlayed(), entity.getTotalScore(), entity.getBestScore(), badgeViews);
    }
}
