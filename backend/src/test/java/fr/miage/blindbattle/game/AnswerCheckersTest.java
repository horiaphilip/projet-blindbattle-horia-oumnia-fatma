package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.AnswerMode;
import fr.miage.blindbattle.domain.GuessTarget;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AnswerCheckersTest {
    private final TypeItChecker typeIt = new TypeItChecker(new AnswerNormalizer(), new Levenshtein());
    private final AnswerCheckers checkers = new AnswerCheckers(List.of(typeIt, new QuickChoiceChecker()));

    private AnswerAttempt typed(String text, GuessTarget target) {
        return new AnswerAttempt(text, null, "Alors on danse - Radio Edit", "Stromae", 0, target);
    }

    @Test void titleWithTypoIsAccepted() { assertThat(typeIt.check(typed("alors on dance", GuessTarget.TITLE))).isEqualTo(1); }

    @Test void wrongTitleIsRejected() { assertThat(typeIt.check(typed("papaoutai", GuessTarget.TITLE))).isZero(); }

    @Test void artistIgnoresCaseAndAccents() { assertThat(typeIt.check(typed("STROMAÉ", GuessTarget.ARTIST))).isEqualTo(1); }

    @Test void titleIsNotAcceptedWhenTheArtistIsExpected() {
        assertThat(typeIt.check(typed("alors on danse", GuessTarget.ARTIST))).isZero();
    }

    @Test void bothInAnyOrderGivesFullPoints() {
        assertThat(typeIt.check(typed("Alors on danse - Stromae", GuessTarget.BOTH))).isEqualTo(1);
        assertThat(typeIt.check(typed("stromae / alors on danse", GuessTarget.BOTH))).isEqualTo(1);
    }

    @Test void bothWithOnlyOneRightPartGivesHalf() {
        assertThat(typeIt.check(typed("alors on danse - angele", GuessTarget.BOTH))).isEqualTo(0.5);
        assertThat(typeIt.check(typed("stromae", GuessTarget.BOTH))).isEqualTo(0.5);
    }

    @Test void emptyAnswerGivesNothing() {
        assertThat(typeIt.check(typed(null, GuessTarget.BOTH))).isZero();
        assertThat(typeIt.check(typed("", GuessTarget.TITLE))).isZero();
    }

    @Test void quickChoiceComparesTheIndex() {
        assertThat(checkers.check(AnswerMode.QUICK_CHOICE, new AnswerAttempt(null, 2, "t", "a", 2, GuessTarget.TITLE))).isEqualTo(1);
        assertThat(checkers.check(AnswerMode.QUICK_CHOICE, new AnswerAttempt(null, 1, "t", "a", 2, GuessTarget.TITLE))).isZero();
        assertThat(checkers.check(AnswerMode.QUICK_CHOICE, new AnswerAttempt(null, null, "t", "a", 2, GuessTarget.TITLE))).isZero();
    }

    @Test void checkersAreSelectedByMode() {
        assertThat(checkers.check(AnswerMode.TYPE_IT, typed("alors on danse", GuessTarget.TITLE))).isEqualTo(1);
        assertThat(checkers.check(AnswerMode.QUICK_CHOICE, typed("alors on danse", GuessTarget.TITLE))).isZero();
    }
}
