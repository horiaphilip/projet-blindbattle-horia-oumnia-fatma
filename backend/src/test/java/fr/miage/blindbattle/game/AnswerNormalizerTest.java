package fr.miage.blindbattle.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

class AnswerNormalizerTest {
    private final AnswerNormalizer normalizer = new AnswerNormalizer();

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "Formidable (Live)|formidable",
        "Alors on danse - Radio Edit|alors on danse",
        "Désenchantée|desenchantee",
        "Bohemian Rhapsody - Remastered 2011|bohemian rhapsody",
        "The Beatles|beatles",
        "Djadja feat. Maluma|djadja",
        "Bad Guy ft. Justin Bieber|bad guy",
        "Thriller [2008 Remaster]|thriller",
        "Don't Stop Me Now|don t stop me now",
        "  Ça   plane pour moi !  |ca plane pour moi",
        "Wonderwall - Remastered|wonderwall",
        "Hey Jude - Remastered 2015|hey jude"
    })
    void normalizes(String raw, String expected) { assertThat(normalizer.normalize(raw)).isEqualTo(expected); }

    @Test void nullBecomesEmpty() { assertThat(normalizer.normalize(null)).isEmpty(); }

    @Test void keepsTitlesThatOnlyContainADash() {
        assertThat(normalizer.normalize("Part 1 - Part 2")).isEqualTo("part 1 part 2");
    }
}
