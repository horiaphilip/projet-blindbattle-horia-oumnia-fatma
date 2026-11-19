package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.AnswerMode;
import org.springframework.stereotype.Component;

@Component
public class TypeItChecker implements AnswerChecker {
    private final AnswerNormalizer normalizer;
    private final Levenshtein levenshtein;

    public TypeItChecker(AnswerNormalizer normalizer, Levenshtein levenshtein) {
        this.normalizer = normalizer;
        this.levenshtein = levenshtein;
    }

    @Override
    public AnswerMode mode() { return AnswerMode.TYPE_IT; }

    @Override
    public double check(AnswerAttempt attempt) {
        String title = normalizer.normalize(attempt.expectedTitle());
        String artist = normalizer.normalize(attempt.expectedArtist());
        return switch (attempt.target()) {
            case TITLE -> matches(attempt.text(), title) ? 1 : 0;
            case ARTIST -> matches(attempt.text(), artist) ? 1 : 0;
            case BOTH -> both(attempt.text(), title, artist);
        };
    }

    private double both(String text, String title, String artist) {
        String[] parts = text == null ? new String[0] : text.split("[-/]", 2);
        if (parts.length == 2) {
            double straight = half(parts[0], title) + half(parts[1], artist);
            double swapped = half(parts[0], artist) + half(parts[1], title);
            return Math.max(straight, swapped);
        }
        return matches(text, title) || matches(text, artist) ? 0.5 : 0;
    }

    private double half(String part, String expected) { return matches(part, expected) ? 0.5 : 0; }

    private boolean matches(String raw, String expected) { return levenshtein.close(normalizer.normalize(raw), expected); }
}
