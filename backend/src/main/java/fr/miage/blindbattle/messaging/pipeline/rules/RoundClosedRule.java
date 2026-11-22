package fr.miage.blindbattle.messaging.pipeline.rules;

import fr.miage.blindbattle.domain.RoundStatus;
import fr.miage.blindbattle.messaging.pipeline.AnswerRule;
import fr.miage.blindbattle.messaging.pipeline.SubmitAnswer;
import fr.miage.blindbattle.repository.RoundRepository;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class RoundClosedRule implements AnswerRule {
    private final RoundRepository rounds;

    public RoundClosedRule(RoundRepository rounds) { this.rounds = rounds; }

    @Override
    public Optional<String> reject(SubmitAnswer answer) {
        boolean open = rounds.findById(answer.roundId()).map(r -> r.getStatus() == RoundStatus.PLAYING).orElse(false);
        return open ? Optional.empty() : Optional.of("La manche est déjà fermée");
    }
}
