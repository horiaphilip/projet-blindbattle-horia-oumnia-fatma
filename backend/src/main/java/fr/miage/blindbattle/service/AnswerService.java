package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.AnswerEntity;
import fr.miage.blindbattle.domain.GameEntity;
import fr.miage.blindbattle.domain.PlayerEntity;
import fr.miage.blindbattle.domain.ProcessedMessageEntity;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.domain.RoundStatus;
import fr.miage.blindbattle.dto.AnswerAccepted;
import fr.miage.blindbattle.dto.AnswerRequest;
import fr.miage.blindbattle.game.AnswerAttempt;
import fr.miage.blindbattle.game.AnswerCheckers;
import fr.miage.blindbattle.game.Score;
import fr.miage.blindbattle.game.ScoringService;
import fr.miage.blindbattle.messaging.GameCommandSender;
import fr.miage.blindbattle.messaging.pipeline.AnswerEnricher;
import fr.miage.blindbattle.messaging.pipeline.AnswerFilter;
import fr.miage.blindbattle.messaging.pipeline.EnrichedAnswer;
import fr.miage.blindbattle.messaging.pipeline.RoundAggregator;
import fr.miage.blindbattle.messaging.pipeline.SubmitAnswer;
import fr.miage.blindbattle.messaging.pipeline.rules.DuplicateAnswerRule;
import fr.miage.blindbattle.repository.AnswerRepository;
import fr.miage.blindbattle.repository.GameRepository;
import fr.miage.blindbattle.repository.PlayerRepository;
import fr.miage.blindbattle.repository.ProcessedMessageRepository;
import fr.miage.blindbattle.repository.RoundRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerService {
    private static final Logger LOG = LoggerFactory.getLogger(AnswerService.class);
    private final GameRepository games;
    private final PlayerRepository players;
    private final RoundRepository rounds;
    private final AnswerRepository answers;
    private final ProcessedMessageRepository processed;
    private final GameCommandSender commands;
    private final AnswerFilter filter;
    private final AnswerEnricher enricher;
    private final AnswerCheckers checkers;
    private final ScoringService scoring;
    private final RoundAggregator aggregator;

    public AnswerService(GameRepository games, PlayerRepository players, RoundRepository rounds, AnswerRepository answers,
            ProcessedMessageRepository processed, GameCommandSender commands, AnswerFilter filter, AnswerEnricher enricher,
            AnswerCheckers checkers, ScoringService scoring, RoundAggregator aggregator) {
        this.games = games;
        this.players = players;
        this.rounds = rounds;
        this.answers = answers;
        this.processed = processed;
        this.commands = commands;
        this.filter = filter;
        this.enricher = enricher;
        this.checkers = checkers;
        this.scoring = scoring;
        this.aggregator = aggregator;
    }

    @Transactional(readOnly = true)
    public AnswerAccepted submit(String code, AnswerRequest request, UUID idempotencyKey) {
        Instant receivedAt = Instant.now();
        GameEntity game = games.findByCode(code).orElseThrow(() -> new GameNotFound(code));
        PlayerEntity player = players.findByToken(request.playerToken())
                .filter(p -> p.getGame().getId().equals(game.getId())).orElseThrow(PlayerNotFound::new);
        RoundEntity round = rounds.findFirstByGameIdAndStatusNotOrderByNumberDesc(game.getId(), RoundStatus.PENDING)
                .orElseThrow(() -> new NoRoundInProgress(code));
        UUID eventId = idempotencyKey == null ? UUID.randomUUID() : idempotencyKey;
        commands.send(code, SubmitAnswer.of(eventId, code, round.getId(), player.getId(), game.getSettings().answerMode(),
                request.text(), request.choiceIndex(), receivedAt));
        return new AnswerAccepted(eventId, round.getNumber());
    }

    @Transactional
    public void process(SubmitAnswer answer) {
        if (processed.existsById(answer.eventId())) {
            LOG.info("Réponse déjà traitée eventId={}, ignorée", answer.eventId());
            return;
        }
        processed.save(new ProcessedMessageEntity(answer.eventId()));
        Optional<String> rejection = filter.reject(answer);
        if (rejection.isPresent()) {
            reject(answer, rejection.get());
            return;
        }
        EnrichedAnswer enriched = enricher.enrich(answer);
        double correctness = checkers.check(answer.mode(), new AnswerAttempt(answer.text(), answer.choiceIndex(),
                enriched.expectedTitle(), enriched.expectedArtist(), enriched.correctChoiceIndex(), enriched.guessTarget()));
        long responseMs = Duration.between(enriched.roundStartedAt(), answer.receivedAt()).toMillis();
        Score score = scoring.score(correctness, responseMs, enriched.roundSeconds() * 1000L, enriched.currentStreak());
        PlayerEntity player = players.findById(answer.playerId()).orElseThrow();
        player.record(score.total(), score.streak());
        answers.save(AnswerEntity.accepted(rounds.getReferenceById(answer.roundId()), player, answer.text(),
                answer.choiceIndex(), answer.receivedAt(), responseMs, correctness, score.total()));
        LOG.info("Réponse de {} : justesse={} points={} temps={}ms", player.getNickname(), correctness, score.total(), responseMs);
        aggregator.collect(answer.roundId());
    }

    private void reject(SubmitAnswer answer, String reason) {
        LOG.info("Réponse rejetée joueur={} manche={} : {}", answer.playerId(), answer.roundId(), reason);
        if (reason.equals(DuplicateAnswerRule.REASON) || !players.existsById(answer.playerId())) return;
        answers.save(AnswerEntity.rejected(rounds.getReferenceById(answer.roundId()), players.getReferenceById(answer.playerId()),
                answer.text(), answer.choiceIndex(), answer.receivedAt(), reason));
        aggregator.collect(answer.roundId());
    }
}
