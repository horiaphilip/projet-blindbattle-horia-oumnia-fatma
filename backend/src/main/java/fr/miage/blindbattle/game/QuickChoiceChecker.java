package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.AnswerMode;
import org.springframework.stereotype.Component;

@Component
public class QuickChoiceChecker implements AnswerChecker {
    @Override
    public AnswerMode mode() { return AnswerMode.QUICK_CHOICE; }

    @Override
    public double check(AnswerAttempt attempt) {
        return attempt.choiceIndex() != null && attempt.choiceIndex() == attempt.correctChoiceIndex() ? 1 : 0;
    }
}
