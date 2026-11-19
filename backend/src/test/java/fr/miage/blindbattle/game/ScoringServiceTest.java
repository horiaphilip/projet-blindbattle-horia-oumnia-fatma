package fr.miage.blindbattle.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {
    private final ScoringService scoring = new ScoringService();

    @ParameterizedTest
    @CsvSource({"0,1000", "10000,750", "20000,500", "25000,500", "-5,1000", "4000,900"})
    void pointsDecreaseWithTime(long responseMs, int expected) {
        assertThat(scoring.score(1, responseMs, 20000, 0).points()).isEqualTo(expected);
    }

    @Test void wrongAnswerGivesNothingAndResetsTheStreak() {
        assertThat(scoring.score(0, 1000, 20000, 4)).isEqualTo(new Score(0, 0, 0, 0));
    }

    @Test void streakBonusGrowsAndIsCapped() {
        assertThat(scoring.score(1, 0, 20000, 0)).isEqualTo(new Score(1000, 0, 1000, 1));
        assertThat(scoring.score(1, 0, 20000, 1)).isEqualTo(new Score(1000, 100, 1100, 2));
        assertThat(scoring.score(1, 0, 20000, 3).bonus()).isEqualTo(300);
        assertThat(scoring.score(1, 0, 20000, 9).bonus()).isEqualTo(300);
    }

    @Test void halfAnswerGivesHalfPointsAndResetsTheStreak() {
        assertThat(scoring.score(0.5, 0, 20000, 3)).isEqualTo(new Score(500, 0, 500, 0));
    }
}
