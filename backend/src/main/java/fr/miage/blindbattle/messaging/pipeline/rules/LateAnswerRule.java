package fr.miage.blindbattle.messaging.pipeline.rules;

import fr.miage.blindbattle.messaging.pipeline.AnswerRule;
import fr.miage.blindbattle.messaging.pipeline.SubmitAnswer;
import fr.miage.blindbattle.repository.RoundRepository;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class LateAnswerRule implements AnswerRule {
    private final RoundRepository rounds;

    public LateAnswerRule(RoundRepository rounds) { this.rounds = rounds; }

    @Override
    public Optional<String> reject(SubmitAnswer answer) {
        boolean late = rounds.findById(answer.roundId())
                .map(r -> r.getEndsAt() == null || answer.receivedAt().isAfter(r.getEndsAt())).orElse(true);
        return late ? Optional.of("Réponse arrivée après la fin de la manche") : Optional.empty();
    }
}
