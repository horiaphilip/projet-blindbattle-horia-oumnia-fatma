package fr.miage.blindbattle.messaging.pipeline;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AnswerFilter {
    private final List<AnswerRule> rules;

    public AnswerFilter(List<AnswerRule> rules) { this.rules = rules; }

    public Optional<String> reject(SubmitAnswer answer) {
        return rules.stream().map(rule -> rule.reject(answer)).flatMap(Optional::stream).findFirst();
    }
}
