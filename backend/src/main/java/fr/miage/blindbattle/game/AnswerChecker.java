package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.AnswerMode;

public interface AnswerChecker {
    AnswerMode mode();
    double check(AnswerAttempt attempt);
}
