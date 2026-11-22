package fr.miage.blindbattle.messaging.pipeline;

import fr.miage.blindbattle.domain.AnswerMode;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.messaging.pipeline.rules.LateAnswerRule;
import fr.miage.blindbattle.repository.RoundRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnswerFilterTest {
    private SubmitAnswer answerReceivedAt(Instant receivedAt) {
        return SubmitAnswer.of(UUID.randomUUID(), "ABCDEF", 1, 1, AnswerMode.QUICK_CHOICE, null, 0, receivedAt);
    }

    @Test void firstFailingRuleGivesTheReason() {
        AnswerFilter filter = new AnswerFilter(List.of(a -> Optional.empty(), a -> Optional.of("première"), a -> Optional.of("seconde")));
        assertThat(filter.reject(answerReceivedAt(Instant.now()))).contains("première");
    }

    @Test void answerPassesWhenNoRuleRejectsIt() {
        AnswerFilter filter = new AnswerFilter(List.of(a -> Optional.empty(), a -> Optional.empty()));
        assertThat(filter.reject(answerReceivedAt(Instant.now()))).isEmpty();
    }

    @Test void lateAnswerIsRejected() {
        RoundEntity round = new RoundEntity(null, null, 1, List.of());
        Instant startedAt = Instant.now().minusSeconds(30);
        round.start(startedAt, 10);
        RoundRepository rounds = mock(RoundRepository.class);
        when(rounds.findById(1L)).thenReturn(Optional.of(round));
        LateAnswerRule rule = new LateAnswerRule(rounds);
        assertThat(rule.reject(answerReceivedAt(startedAt.plusSeconds(9)))).isEmpty();
        assertThat(rule.reject(answerReceivedAt(startedAt.plusSeconds(11)))).contains("Réponse arrivée après la fin de la manche");
    }
}
