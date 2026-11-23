package fr.miage.blindbattle.integration.deezer;

import fr.miage.blindbattle.domain.TrackEntity;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DeezerTrackTranslatorTest {
    private final DeezerTrackTranslator translator = new DeezerTrackTranslator();
    private final DeezerArtist stromae = new DeezerArtist(5542, "Stromae", "picture.jpg");
    private final DeezerAlbum cheese = new DeezerAlbum(302127, "Cheese", "cover.jpg");

    @Test void mapsEveryField() {
        DeezerTrack deezer = new DeezerTrack(3135556L, "Alors on danse", "preview.mp3", 912345, 206, stromae, cheese);
        TrackEntity track = translator.toEntity(deezer);
        assertThat(track.getDeezerId()).isEqualTo(3135556);
        assertThat(track.getTitle()).isEqualTo("Alors on danse");
        assertThat(track.getArtist()).isEqualTo("Stromae");
        assertThat(track.getArtistId()).isEqualTo(5542);
        assertThat(track.getAlbum()).isEqualTo("Cheese");
        assertThat(track.getCoverUrl()).isEqualTo("cover.jpg");
        assertThat(track.getRank()).isEqualTo(912345);
        assertThat(track.getDurationSeconds()).isEqualTo(206);
        assertThat(track.hasPreview()).isTrue();
    }

    @Test void emptyPreviewMeansNoPreview() {
        assertThat(translator.toEntity(new DeezerTrack(1L, "Titre", "", 1, 30, stromae, cheese)).hasPreview()).isFalse();
        assertThat(translator.toEntity(new DeezerTrack(2L, "Titre", null, 1, 30, stromae, cheese)).hasPreview()).isFalse();
    }

    @Test void missingArtistAndAlbum() {
        TrackEntity track = translator.toEntity(new DeezerTrack(3L, "Titre", "preview.mp3", 1, 30, null, null));
        assertThat(track.getArtist()).isEqualTo("Artiste inconnu");
        assertThat(track.getAlbum()).isNull();
        assertThat(track.getCoverUrl()).isNull();
    }

    @Test void refreshUpdatesTheEntity() {
        TrackEntity track = translator.toEntity(new DeezerTrack(4L, "Titre", "preview.mp3", 100, 30, stromae, cheese));
        translator.refresh(track, new DeezerTrack(4L, "Titre (Remix)", "", 200, 31, stromae, cheese));
        assertThat(track.getTitle()).isEqualTo("Titre (Remix)");
        assertThat(track.getRank()).isEqualTo(200);
        assertThat(track.hasPreview()).isFalse();
    }
}
