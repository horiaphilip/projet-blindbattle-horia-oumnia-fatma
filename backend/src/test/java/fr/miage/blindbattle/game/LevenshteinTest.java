package fr.miage.blindbattle.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

class LevenshteinTest {
    private final Levenshtein levenshtein = new Levenshtein();

    @ParameterizedTest
    @CsvSource({"kitten,sitting,3", "stromae,stromae,0", "stromae,stromea,2", "abc,'',3", "'',abc,3", "flaw,lawn,2"})
    void computesDistance(String a, String b, int expected) { assertThat(levenshtein.distance(a, b)).isEqualTo(expected); }

    @ParameterizedTest
    @CsvSource({"formidabl,formidable", "formidable,formidable", "papaoutai,papaoutai", "despacitto,despacito", "a,b"})
    void acceptsSmallTypos(String answer, String expected) { assertThat(levenshtein.close(answer, expected)).isTrue(); }

    @ParameterizedTest
    @CsvSource({"formi,formidable", "thriller,billie jean", "abcd,ab"})
    void rejectsDifferentAnswers(String answer, String expected) { assertThat(levenshtein.close(answer, expected)).isFalse(); }

    @Test void emptyAnswerIsNeverClose() {
        assertThat(levenshtein.close("", "a")).isFalse();
        assertThat(levenshtein.close("a", "")).isFalse();
    }
}
