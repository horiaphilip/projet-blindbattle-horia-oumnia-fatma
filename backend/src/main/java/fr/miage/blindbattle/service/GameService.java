package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.AnswerMode;
import fr.miage.blindbattle.domain.GameEntity;
import fr.miage.blindbattle.domain.GameSettings;
import fr.miage.blindbattle.domain.GameStatus;
import fr.miage.blindbattle.domain.PlayerEntity;
import fr.miage.blindbattle.domain.QuizEntity;
import fr.miage.blindbattle.domain.QuizTrackEntity;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.domain.RoundStatus;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.domain.UserEntity;
import fr.miage.blindbattle.dto.GameRequest;
import fr.miage.blindbattle.dto.GameStateView;
import fr.miage.blindbattle.dto.GameView;
import fr.miage.blindbattle.dto.JoinRequest;
import fr.miage.blindbattle.dto.JoinView;
import fr.miage.blindbattle.dto.PlayerView;
import fr.miage.blindbattle.dto.PreviewView;
import fr.miage.blindbattle.game.DistractorPicker;
import fr.miage.blindbattle.messaging.GameEvent;
import fr.miage.blindbattle.messaging.GameEventType;
import fr.miage.blindbattle.messaging.SseBroadcaster;
import fr.miage.blindbattle.repository.GameRepository;
import fr.miage.blindbattle.repository.PlayerRepository;
import fr.miage.blindbattle.repository.QuizRepository;
import fr.miage.blindbattle.repository.RoundRepository;
import fr.miage.blindbattle.repository.UserRepository;
import fr.miage.blindbattle.security.AuthUser;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class GameService {
    private final GameRepository games;
    private final PlayerRepository players;
    private final RoundRepository rounds;
    private final UserRepository users;
    private final QuizRepository quizzes;
    private final CatalogueService catalogue;
    private final GameCodeGenerator codes;
    private final DistractorPicker distractors;
    private final RoundService roundService;
    private final SseBroadcaster broadcaster;
    private final ApplicationEventPublisher events;

    public GameService(GameRepository games, PlayerRepository players, RoundRepository rounds, UserRepository users,
            QuizRepository quizzes, CatalogueService catalogue, GameCodeGenerator codes, DistractorPicker distractors, RoundService roundService,
            SseBroadcaster broadcaster, ApplicationEventPublisher events) {
        this.games = games;
        this.players = players;
        this.rounds = rounds;
        this.users = users;
        this.quizzes = quizzes;
        this.catalogue = catalogue;
        this.codes = codes;
        this.distractors = distractors;
        this.roundService = roundService;
        this.broadcaster = broadcaster;
        this.events = events;
    }

    @Transactional
    public GameView create(AuthUser host, GameRequest request) {
        UserEntity user = users.findById(host.id()).orElseThrow(() -> new UserNotFound(host.id()));
        checkSource(request.settings(), host);
        String code = codes.next();
        while (games.existsByCode(code)) code = codes.next();
        return view(games.save(new GameEntity(code, user, request.settings())));
    }

    @Transactional(readOnly = true)
    public GameView get(String code) { return view(games.findWithPlayersByCode(code).orElseThrow(() -> new GameNotFound(code))); }

    @Transactional
    public JoinView join(String code, AuthUser user, JoinRequest request) {
        GameEntity game = required(code);
        if (game.getStatus() != GameStatus.LOBBY) throw new GameAlreadyStarted(code);
        String nickname = request.nickname().strip();
        if (players.existsByGameIdAndNicknameIgnoreCase(game.getId(), nickname)) throw new NicknameTaken(nickname);
        UserEntity account = user == null ? null : users.getReferenceById(user.id());
        PlayerEntity player = players.save(new PlayerEntity(game, account, nickname));
        events.publishEvent(GameEvent.of(GameEventType.PLAYER_JOINED, code, Map.of("playerId", player.getId(),
                "nickname", player.getNickname(), "guest", player.isGuest(), "playerCount", players.countByGameId(game.getId()))));
        return new JoinView(player.getId(), player.getNickname(), player.getToken());
    }

    @Transactional(readOnly = true)
    public SseEmitter subscribe(String code) {
        required(code);
        return broadcaster.subscribe(code);
    }

    @Transactional
    public GameStateView start(String code, AuthUser host) {
        GameEntity game = required(code);
        if (!game.isHost(host.id())) throw new NotHost();
        if (game.getStatus() != GameStatus.LOBBY) throw new GameAlreadyStarted(code);
        if (players.countByGameId(game.getId()) == 0) throw new NoPlayers(code);
        GameSettings settings = game.getSettings();
        List<TrackEntity> pool = distinctPool(settings);
        if (pool.size() < settings.roundCount()) throw new TrackPoolTooSmall(pool.size(), settings.roundCount());
        List<TrackEntity> ordered = new ArrayList<>(pool);
        if (!settings.fromQuiz()) Collections.shuffle(ordered, ThreadLocalRandom.current());
        ordered.subList(0, settings.roundCount()).forEach(track -> game.addRound(track, choicesFor(track, pool, settings)));
        game.start();
        games.saveAndFlush(game);
        events.publishEvent(GameEvent.of(GameEventType.GAME_STARTED, code, Map.of("totalRounds", settings.roundCount())));
        roundService.startNext(code);
        return state(game);
    }

    @Transactional(readOnly = true)
    public GameStateView state(String code) { return state(required(code)); }

    @Transactional(readOnly = true)
    public PreviewView preview(String code, AuthUser host) {
        GameEntity game = required(code);
        if (host == null || !game.isHost(host.id())) throw new NotHost();
        RoundEntity round = rounds.findFirstByGameIdAndStatusOrderByNumberAsc(game.getId(), RoundStatus.PLAYING)
                .orElseThrow(() -> new NoRoundInProgress(code));
        return new PreviewView(round.getId(), round.getNumber(), catalogue.freshPreview(round.getTrack().getDeezerId()));
    }

    private void checkSource(GameSettings settings, AuthUser host) {
        if (settings.fromQuiz()) {
            QuizEntity quiz = quiz(settings.quizId());
            if (!quiz.isPublic() && !quiz.isOwnedBy(host.id())) throw new NotQuizOwner();
        } else if (settings.themeId() == null || settings.themeId().isBlank()) {
            throw new GameSourceMissing();
        }
    }

    private QuizEntity quiz(long id) { return quizzes.findWithTracksById(id).orElseThrow(() -> new QuizNotFound(id)); }

    private List<TrackEntity> distinctPool(GameSettings settings) {
        Map<Long, TrackEntity> distinct = new LinkedHashMap<>();
        source(settings).forEach(t -> distinct.putIfAbsent(t.getDeezerId(), t));
        return List.copyOf(distinct.values());
    }

    private List<TrackEntity> source(GameSettings settings) {
        if (!settings.fromQuiz()) return catalogue.pool(settings.themeId(), settings.difficulty());
        return quiz(settings.quizId()).getTracks().stream().map(QuizTrackEntity::getTrack).filter(TrackEntity::hasPreview).toList();
    }

    private List<Long> choicesFor(TrackEntity track, List<TrackEntity> pool, GameSettings settings) {
        if (settings.answerMode() != AnswerMode.QUICK_CHOICE) return List.of();
        return distractors.choices(track, pool, settings.guessTarget(), ThreadLocalRandom.current()).tracks().stream()
                .map(TrackEntity::getId).toList();
    }

    private GameStateView state(GameEntity game) {
        Instant endsAt = rounds.findFirstByGameIdAndStatusOrderByNumberAsc(game.getId(), RoundStatus.PLAYING)
                .map(RoundEntity::getEndsAt).orElse(null);
        int currentRound = (int) rounds.countByGameIdAndStatusNot(game.getId(), RoundStatus.PENDING);
        return new GameStateView(game.getCode(), game.getStatus().name(), currentRound, game.getSettings().roundCount(),
                endsAt, Instant.now(), players.countByGameId(game.getId()));
    }

    private GameEntity required(String code) { return games.findByCode(code).orElseThrow(() -> new GameNotFound(code)); }

    private GameView view(GameEntity game) {
        List<PlayerView> playerViews = game.getPlayers().stream()
                .map(p -> new PlayerView(p.getId(), p.getNickname(), p.getScore(), p.isGuest()))
                .toList();
        return new GameView(game.getCode(), game.getStatus().name(), game.getSettings(), game.getHost().getId(),
                game.getHost().getName(), playerViews, game.getCreatedAt());
    }
}
