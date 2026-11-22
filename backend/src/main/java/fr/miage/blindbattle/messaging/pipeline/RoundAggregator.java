package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.messaging.GameEvent;
import fr.miage.blindbattle.messaging.GameEventType;
import fr.miage.blindbattle.repository.AnswerRepository;
import fr.miage.blindbattle.repository.PlayerRepository;
import fr.miage.blindbattle.repository.RoundRepository;
import fr.miage.blindbattle.service.RoundService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoundAggregator {
    private static final Logger LOG = LoggerFactory.getLogger(RoundAggregator.class);
    private final RoundRepository rounds;
    private final AnswerRepository answers;
    private final PlayerRepository players;
    private final RoundService roundService;
    private final ApplicationEventPublisher events;

    public RoundAggregator(RoundRepository rounds, AnswerRepository answers, PlayerRepository players,
            RoundService roundService, ApplicationEventPublisher events) {
        this.rounds = rounds;
        this.answers = answers;
        this.players = players;
        this.roundService = roundService;
        this.events = events;
    }

    @Transactional
    public void collect(long roundId) {
        RoundEntity round = rounds.findById(roundId).orElseThrow();
        String code = round.getGame().getCode();
        long answered = answers.countByRoundId(roundId);
        long expected = players.countByGameId(round.getGame().getId());
        events.publishEvent(GameEvent.of(GameEventType.ANSWER_COUNT, code,
                Map.of("roundId", roundId, "answered", answered, "players", expected)));
        if (answered >= expected) {
            LOG.info("Manche {} complète ({}/{}) partie={}", round.getNumber(), answered, expected, code);
            roundService.end(roundId);
        }
    }

    @Transactional
    public void timeout(long roundId) {
        LOG.info("Fin du temps pour la manche id={}", roundId);
        roundService.end(roundId);
    }
}
