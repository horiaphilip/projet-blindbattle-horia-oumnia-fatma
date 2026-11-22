package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.GameEntity;
import fr.miage.blindbattle.domain.GameSettings;
import fr.miage.blindbattle.domain.GameStatus;
import fr.miage.blindbattle.domain.PlayerEntity;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.domain.RoundStatus;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.game.Choices;
import fr.miage.blindbattle.game.DistractorPicker;
import fr.miage.blindbattle.messaging.GameEvent;
import fr.miage.blindbattle.messaging.GameEventType;
import fr.miage.blindbattle.repository.AnswerRepository;
import fr.miage.blindbattle.repository.GameRepository;
import fr.miage.blindbattle.repository.PlayerRepository;
import fr.miage.blindbattle.repository.RoundRepository;
import fr.miage.blindbattle.repository.TrackRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoundService {
    private final GameRepository games;
    private final RoundRepository rounds;
    private final PlayerRepository players;
    private final AnswerRepository answers;
    private final TrackRepository tracks;
    private final DistractorPicker distractors;
    private final RoundScheduler scheduler;
    private final ApplicationEventPublisher events;

    public RoundService(GameRepository games, RoundRepository rounds, PlayerRepository players, AnswerRepository answers,
            TrackRepository tracks, DistractorPicker distractors, RoundScheduler scheduler, ApplicationEventPublisher events) {
        this.games = games;
        this.rounds = rounds;
        this.players = players;
        this.answers = answers;
        this.tracks = tracks;
        this.distractors = distractors;
        this.scheduler = scheduler;
        this.events = events;
    }

    @Transactional
    public void startNext(String gameCode) {
        GameEntity game = games.findByCode(gameCode).orElseThrow(() -> new GameNotFound(gameCode));
        if (game.getStatus() != GameStatus.PLAYING) return;
        RoundEntity round = rounds.findFirstByGameIdAndStatusOrderByNumberAsc(game.getId(), RoundStatus.PENDING).orElse(null);
        if (round == null) {
            finish(game);
            return;
        }
        GameSettings settings = game.getSettings();
        Instant now = Instant.now();
        round.start(now, settings.roundSeconds());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("roundId", round.getId());
        data.put("roundNumber", round.getNumber());
        data.put("totalRounds", settings.roundCount());
        data.put("startedAt", round.getStartedAt());
        data.put("endsAt", round.getEndsAt());
        data.put("serverTime", now);
        data.put("choices", choiceLabels(round, settings));
        events.publishEvent(GameEvent.of(GameEventType.ROUND_STARTED, gameCode, data));
        scheduler.timeoutAt(gameCode, round.getId(), round.getEndsAt());
    }

    @Transactional
    public void end(long roundId) {
        RoundEntity round = rounds.findById(roundId).orElse(null);
        if (round == null || round.getStatus() != RoundStatus.PLAYING) return;
        round.end();
        GameEntity game = round.getGame();
        TrackEntity track = round.getTrack();
        List<Long> answered = answers.findPlayerIdsByRoundId(roundId);
        players.findByGameIdOrderByScoreDescJoinedAtAsc(game.getId()).stream()
                .filter(p -> !answered.contains(p.getId())).forEach(PlayerEntity::resetStreak);
        String fastest = answers.findFirstByRoundIdAndCorrectnessGreaterThanOrderByResponseMsAsc(roundId, 0)
                .map(a -> a.getPlayer().getNickname()).orElse(null);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("roundId", round.getId());
        data.put("roundNumber", round.getNumber());
        data.put("totalRounds", game.getSettings().roundCount());
        data.put("title", track.getTitle());
        data.put("artist", track.getArtist());
        data.put("coverUrl", track.getCoverUrl());
        data.put("fastest", fastest);
        data.put("leaderboard", leaderboard(game));
        events.publishEvent(GameEvent.of(GameEventType.ROUND_ENDED, game.getCode(), data));
        scheduler.nextRoundAfterPause(game.getCode());
    }

    private void finish(GameEntity game) {
        game.finish();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("leaderboard", leaderboard(game));
        events.publishEvent(GameEvent.of(GameEventType.GAME_FINISHED, game.getCode(), data));
    }

    private List<String> choiceLabels(RoundEntity round, GameSettings settings) {
        if (round.getChoiceTrackIds().isEmpty()) return List.of();
        Map<Long, TrackEntity> byId = tracks.findAllById(round.getChoiceTrackIds()).stream()
                .collect(Collectors.toMap(TrackEntity::getId, Function.identity()));
        List<TrackEntity> ordered = round.getChoiceTrackIds().stream().map(byId::get).toList();
        return distractors.labels(new Choices(ordered, ordered.indexOf(round.getTrack())), settings.guessTarget());
    }

    private List<Map<String, Object>> leaderboard(GameEntity game) {
        return players.findByGameIdOrderByScoreDescJoinedAtAsc(game.getId()).stream()
                .map(p -> Map.<String, Object>of("playerId", p.getId(), "nickname", p.getNickname(), "score", p.getScore()))
                .toList();
    }
}
