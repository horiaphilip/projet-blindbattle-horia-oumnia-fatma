package fr.miage.blindbattle.messaging.pipeline;

import java.util.Optional;

public interface AnswerRule {
    Optional<String> reject(SubmitAnswer answer);
}
