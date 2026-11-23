package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.integration.deezer.DeezerAlbum;
import fr.miage.blindbattle.integration.deezer.DeezerArtist;
import fr.miage.blindbattle.integration.deezer.DeezerTrack;
import fr.miage.blindbattle.repository.TrackRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:tracks", "spring.jpa.hibernate.ddl-auto=create-drop"})
class TrackServiceTest {
    @Autowired TrackService service;
    @Autowired TrackRepository repository;

    private final DeezerArtist stromae = new DeezerArtist(5542, "Stromae", "picture.jpg");
    private final DeezerAlbum cheese = new DeezerAlbum(302127, "Cheese", "cover.jpg");

    @BeforeEach void clean() { repository.deleteAll(); }

    @Test void savingTwiceKeepsOneRowAndRefreshesRank() {
        service.saveAll(List.of(track(3135556, 900000)));
        service.saveAll(List.of(track(3135556, 950000)));
        assertThat(repository.findAll()).singleElement().extracting(TrackEntity::getRank).isEqualTo(950000);
    }

    @Test void keepsTheInputOrder() {
        List<TrackEntity> saved = service.saveAll(List.of(track(3, 10), track(1, 30), track(2, 20)));
        assertThat(saved).extracting(TrackEntity::getDeezerId).containsExactly(3L, 1L, 2L);
        assertThat(saved).allSatisfy(track -> assertThat(track.getId()).isNotNull());
    }

    @Test void duplicateInTheSameCallIsStoredOnce() {
        service.saveAll(List.of(track(7, 10), track(7, 20)));
        assertThat(repository.findByDeezerId(7)).get().extracting(TrackEntity::getRank).isEqualTo(20);
        assertThat(repository.count()).isEqualTo(1);
    }

    private DeezerTrack track(long id, int rank) {
        return new DeezerTrack(id, "Titre " + id, "preview.mp3", rank, 200, stromae, cheese);
    }
}
