package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.AuditLogEntity;
import fr.miage.blindbattle.domain.BadgeEntity;
import fr.miage.blindbattle.domain.GameEntity;
import fr.miage.blindbattle.domain.GameResultEntity;
import fr.miage.blindbattle.domain.PlayerEntity;
import fr.miage.blindbattle.domain.ProcessedMessageEntity;
import fr.miage.blindbattle.domain.UserEntity;
import fr.miage.blindbattle.domain.UserStatsEntity;
import fr.miage.blindbattle.dto.HistoryEntryView;
import fr.miage.blindbattle.dto.LeaderboardEntryView;
import fr.miage.blindbattle.dto.PageView;
import fr.miage.blindbattle.dto.PodiumEntryView;
import fr.miage.blindbattle.messaging.PlayerResult;
import fr.miage.blindbattle.repository.AnswerRepository;
import fr.miage.blindbattle.repository.AuditLogRepository;
import fr.miage.blindbattle.repository.BadgeRepository;
import fr.miage.blindbattle.repository.GameRepository;
import fr.miage.blindbattle.repository.GameResultRepository;
import fr.miage.blindbattle.repository.PlayerRepository;
import fr.miage.blindbattle.repository.ProcessedMessageRepository;
import fr.miage.blindbattle.repository.UserRepository;
import fr.miage.blindbattle.repository.UserStatsRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameResultService {
    public static final String FIRST_GAME_BADGE = "FIRST_GAME";
    public static final String WINNER_BADGE = "WINNER";
    private static final Logger LOG = LoggerFactory.getLogger(GameResultService.class);
    private static final int MAX_PAGE_SIZE = 50;
    private final GameRepository games;
    private final PlayerRepository players;
    private final AnswerRepository answers;
    private final GameResultRepository results;
    private final UserRepository users;
    private final UserStatsRepository stats;
    private final BadgeRepository badges;
    private final AuditLogRepository audit;
    private final ProcessedMessageRepository processed;

    public GameResultService(GameRepository games, PlayerRepository players, AnswerRepository answers,
            GameResultRepository results, UserRepository users, UserStatsRepository stats, BadgeRepository badges,
            AuditLogRepository audit, ProcessedMessageRepository processed) {
        this.games = games;
        this.players = players;
        this.answers = answers;
        this.results = results;
        this.users = users;
        this.stats = stats;
        this.badges = badges;
        this.audit = audit;
        this.processed = processed;
    }

    @Transactional(readOnly = true)
    public List<PlayerResult> split(String gameCode, UUID sourceEventId) {
        GameEntity game = games.findByCode(gameCode).orElseThrow(() -> new GameNotFound(gameCode));
        List<PlayerEntity> ranking = players.findByGameIdOrderByScoreDescJoinedAtAsc(game.getId());
        List<PlayerResult> split = new ArrayList<>();
        int rank = 0;
        for (int i = 0; i < ranking.size(); i++) {
            PlayerEntity player = ranking.get(i);
            if (i == 0 || player.getScore() < ranking.get(i - 1).getScore()) rank = i + 1;
            UUID eventId = UUID.nameUUIDFromBytes((sourceEventId + ":" + player.getId()).getBytes(StandardCharsets.UTF_8));
            Long userId = player.getUser() == null ? null : player.getUser().getId();
            split.add(new PlayerResult(eventId, gameCode, game.getId(), player.getId(), userId, player.getNickname(), rank,
                    ranking.size(), player.getScore(), (int) answers.countCorrectByPlayerId(player.getId()),
                    Math.round(answers.averageResponseMsByPlayerId(player.getId()))));
        }
        return split;
    }

    @Transactional
    public void record(PlayerResult result) {
        if (processed.existsById(result.eventId())) {
            LOG.info("Résultat {} déjà traité, ignoré", result.eventId());
            return;
        }
        processed.save(new ProcessedMessageEntity(result.eventId()));
        if (results.existsByGameIdAndPlayerId(result.gameId(), result.playerId())) return;
        results.save(new GameResultEntity(games.getReferenceById(result.gameId()), players.getReferenceById(result.playerId()),
                result.rank(), result.finalScore(), result.correctAnswers(), result.avgResponseMs()));
        if (result.userId() == null || !users.existsById(result.userId())) return;
        UserEntity user = users.getReferenceById(result.userId());
        UserStatsEntity userStats = stats.findByUserId(result.userId()).orElseGet(() -> stats.save(new UserStatsEntity(user)));
        userStats.recordGame(result.finalScore());
        award(user, FIRST_GAME_BADGE);
        if (result.rank() == 1 && result.playerCount() > 1) award(user, WINNER_BADGE);
        audit.save(new AuditLogEntity("GAME_RESULT", result.userId(), result.nickname() + " termine " + result.rank()
                + "e sur " + result.playerCount() + " de la partie " + result.gameCode() + " avec " + result.finalScore() + " points"));
        LOG.info("Statistiques mises à jour pour userId={} partie={} rang={}", result.userId(), result.gameCode(), result.rank());
    }

    @Transactional(readOnly = true)
    public List<PodiumEntryView> podium(String gameCode) {
        if (!games.existsByCode(gameCode)) throw new GameNotFound(gameCode);
        return results.findPodium(gameCode).stream()
                .map(r -> new PodiumEntryView(r.getRank(), r.getNickname(), r.getFinalScore(), r.getCorrectAnswers(), r.getAvgResponseMs()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageView<LeaderboardEntryView> leaderboard(int page, int size) {
        Page<LeaderboardEntryView> rows = results.findLeaderboard(pageRequest(page, size))
                .map(r -> new LeaderboardEntryView(r.getUserId(), r.getName(), r.getGamesPlayed(), r.getTotalScore(),
                        r.getBestScore(), r.getWins()));
        return pageView(rows);
    }

    @Transactional(readOnly = true)
    public PageView<HistoryEntryView> history(long userId, int page, int size) {
        if (!users.existsById(userId)) throw new UserNotFound(userId);
        Page<HistoryEntryView> rows = results.findHistory(userId, pageRequest(page, size))
                .map(r -> new HistoryEntryView(r.getGameCode(), r.getPlayedAt(), r.getRank(), r.getPlayerCount(),
                        r.getFinalScore(), r.getCorrectAnswers(), r.getTotalRounds()));
        return pageView(rows);
    }

    private void award(UserEntity user, String code) {
        if (!badges.existsByUserIdAndCode(user.getId(), code)) badges.save(new BadgeEntity(user, code));
    }

    private PageRequest pageRequest(int page, int size) { return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE)); }

    private <T> PageView<T> pageView(Page<T> rows) {
        return new PageView<>(rows.getContent(), rows.getNumber(), rows.getSize(), rows.getTotalElements(), rows.getTotalPages());
    }
}
