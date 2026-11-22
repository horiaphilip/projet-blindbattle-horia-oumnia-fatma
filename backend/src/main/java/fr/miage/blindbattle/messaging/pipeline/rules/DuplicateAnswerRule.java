package fr.miage.blindbattle.messaging.pipeline.rules;

import fr.miage.blindbattle.messaging.pipeline.AnswerRule;
import fr.miage.blindbattle.messaging.pipeline.SubmitAnswer;
import fr.miage.blindbattle.repository.AnswerRepository;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class DuplicateAnswerRule implements AnswerRule {
    public static final String REASON = "Le joueur a déjà répondu à cette manche";
    private final AnswerRepository answers;

    public DuplicateAnswerRule(AnswerRepository answers) { this.answers = answers; }

    @Override
    public Optional<String> reject(SubmitAnswer answer) {
        return answers.existsByRoundIdAndPlayerId(answer.roundId(), answer.playerId()) ? Optional.of(REASON) : Optional.empty();
    }
}
