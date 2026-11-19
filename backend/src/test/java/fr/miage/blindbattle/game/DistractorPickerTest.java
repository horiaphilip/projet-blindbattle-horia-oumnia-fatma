package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.GuessTarget;
import fr.miage.blindbattle.domain.TrackEntity;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistractorPickerTest {
    private final DistractorPicker picker = new DistractorPicker();
    private final Random random = new Random(42);

    private TrackEntity track(long id, String title, String artist) {
        return new TrackEntity(id, title, artist, id, "Album", null, 1000, 30, true);
    }

    private final TrackEntity answer = track(1, "Formidable", "Stromae");
    private final List<TrackEntity> pool = List.of(answer,
            track(2, "Papaoutai", "Stromae"), track(3, "Tout oublier", "Angèle"), track(4, "Balance ton quoi", "Angèle"),
            track(5, "Djadja", "Aya Nakamura"), track(6, "Copines", "Aya Nakamura"), track(7, "Bella", "Maître Gims"),
            track(8, "formidable", "Charles Aznavour"));

    @RepeatedTest(20) void fourDistinctTitlesIncludingTheAnswer() {
        Choices choices = picker.choices(answer, pool, GuessTarget.TITLE, random);
        assertThat(choices.tracks()).hasSize(4);
        assertThat(choices.tracks().get(choices.correctIndex())).isSameAs(answer);
        assertThat(choices.tracks()).extracting(t -> t.getTitle().toLowerCase()).doesNotHaveDuplicates();
    }

    @RepeatedTest(20) void distinctArtistsWhenGuessingTheArtist() {
        Choices choices = picker.choices(answer, pool, GuessTarget.ARTIST, random);
        assertThat(choices.tracks()).extracting(TrackEntity::getArtist).doesNotHaveDuplicates().contains("Stromae");
        assertThat(choices.tracks().get(choices.correctIndex())).isSameAs(answer);
    }

    @Test void failsWhenThePoolIsTooSmall() {
        List<TrackEntity> small = List.of(answer, track(2, "Papaoutai", "Stromae"), track(3, "Tout oublier", "Angèle"),
                track(4, "Balance ton quoi", "Angèle"));
        assertThatThrownBy(() -> picker.choices(answer, small, GuessTarget.ARTIST, random)).isInstanceOf(NotEnoughTracks.class);
    }

    @Test void labelsFollowTheGuessTarget() {
        Choices choices = new Choices(List.of(answer, track(5, "Djadja", "Aya Nakamura")), 0);
        assertThat(picker.labels(choices, GuessTarget.TITLE)).containsExactly("Formidable", "Djadja");
        assertThat(picker.labels(choices, GuessTarget.ARTIST)).containsExactly("Stromae", "Aya Nakamura");
        assertThat(picker.labels(choices, GuessTarget.BOTH)).containsExactly("Formidable – Stromae", "Djadja – Aya Nakamura");
    }
}
