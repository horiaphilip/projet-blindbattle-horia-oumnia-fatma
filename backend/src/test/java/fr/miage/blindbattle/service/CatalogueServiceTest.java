package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.Difficulty;
import fr.miage.blindbattle.domain.TrackEntity;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CatalogueServiceTest {
    private final CatalogueService catalogue = new CatalogueService(null);
    private final List<TrackEntity> tracks = IntStream.rangeClosed(1, 9)
            .mapToObj(i -> new TrackEntity(i, "Titre " + i, "Artiste " + i, i, "Album", null, i * 1000, 30, true)).toList();

    @Test void easyKeepsMostPopularThird() {
        assertThat(catalogue.byDifficulty(tracks, Difficulty.EASY)).extracting(TrackEntity::getRank).containsExactly(9000, 8000, 7000);
    }

    @Test void hardKeepsTheRest() {
        assertThat(catalogue.byDifficulty(tracks, Difficulty.HARD)).hasSize(6).extracting(TrackEntity::getRank).doesNotContain(9000);
    }

    @Test void tracksWithoutPreviewAreExcluded() {
        TrackEntity silent = new TrackEntity(99, "Muet", "Personne", 99, "Album", null, 99000, 30, false);
        assertThat(catalogue.byDifficulty(List.of(silent), Difficulty.EASY)).isEmpty();
    }
}
