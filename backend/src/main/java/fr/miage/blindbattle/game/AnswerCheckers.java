package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.AnswerMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AnswerCheckers {
    private final Map<AnswerMode, AnswerChecker> checkers;

    public AnswerCheckers(List<AnswerChecker> checkers) {
        this.checkers = checkers.stream().collect(Collectors.toMap(AnswerChecker::mode, Function.identity()));
    }

    public double check(AnswerMode mode, AnswerAttempt attempt) { return checkers.get(mode).check(attempt); }
}
