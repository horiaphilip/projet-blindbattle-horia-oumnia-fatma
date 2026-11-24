package fr.miage.blindbattle;

import fr.miage.blindbattle.domain.Difficulty;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.integration.deezer.DeezerAlbum;
import fr.miage.blindbattle.integration.deezer.DeezerArtist;
import fr.miage.blindbattle.integration.deezer.DeezerClient;
import fr.miage.blindbattle.integration.deezer.DeezerTrack;
import fr.miage.blindbattle.integration.deezer.DeezerUnavailable;
import fr.miage.blindbattle.service.CatalogueService;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:catalogue", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cache.type=none"})
@AutoConfigureMockMvc
class CatalogueApiTest {
    private static final DeezerArtist STROMAE = new DeezerArtist(5542, "Stromae", "stromae.jpg");
    private static final DeezerAlbum ALBUM = new DeezerAlbum(1, "Album", "cover.jpg");

    @Autowired MockMvc mvc;
    @Autowired CatalogueService catalogue;
    @MockitoBean DeezerClient deezer;

    @Test void listThemes() throws Exception {
        when(deezer.playlistTracks(anyLong())).thenReturn(nine(100));
        when(deezer.chartTracks(anyInt())).thenReturn(nine(200));
        mvc.perform(get("/themes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(9)))
            .andExpect(jsonPath("$[0].id").value("chart-world"))
            .andExpect(jsonPath("$[0].category").value("Classements"))
            .andExpect(jsonPath("$[0].coverUrl").value("cover.jpg"));
    }

    @Test void themeEasyKeepsMostPopular() throws Exception {
        when(deezer.playlistTracks(anyLong())).thenReturn(nine(300));
        mvc.perform(get("/themes/genre-rap").param("difficulty", "EASY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.theme.name").value("Rap"))
            .andExpect(jsonPath("$.difficulty").value("EASY"))
            .andExpect(jsonPath("$.tracks[*].rank", contains(9000, 8000, 7000)))
            .andExpect(jsonPath("$.tracks[0].id").isNumber());
    }

    @Test void themeHardKeepsTheRest() throws Exception {
        when(deezer.playlistTracks(anyLong())).thenReturn(nine(400));
        mvc.perform(get("/themes/genre-rock").param("difficulty", "HARD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tracks", hasSize(6)))
            .andExpect(jsonPath("$.tracks[*].rank", not(hasItem(9000))));
    }

    @Test void tracksWithoutPreviewAreExcludedFromPool() {
        when(deezer.playlistTracks(anyLong())).thenReturn(List.of(track(501, 9000, ""), track(502, 8000, "preview.mp3"),
                track(503, 7000, null)));
        List<TrackEntity> easy = catalogue.pool("genre-pop", Difficulty.EASY);
        List<TrackEntity> hard = catalogue.pool("genre-pop", Difficulty.HARD);
        assertThat(easy).extracting(TrackEntity::getDeezerId).containsExactly(502L);
        assertThat(hard).isEmpty();
    }

    @Test void artistTheme() throws Exception {
        when(deezer.artistTop(5542)).thenReturn(nine(600));
        mvc.perform(get("/themes/artist-5542"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.theme.name").value("100% Stromae"))
            .andExpect(jsonPath("$.theme.category").value("Artiste"));
    }

    @Test void searchTracksFlagsMissingPreview() throws Exception {
        when(deezer.searchTracks("stromae")).thenReturn(List.of(track(701, 5000, "preview.mp3"), track(702, 4000, "")));
        mvc.perform(get("/tracks/search").param("q", "stromae"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].hasPreview").value(true))
            .andExpect(jsonPath("$[1].hasPreview").value(false))
            .andExpect(jsonPath("$[1].id").isNumber())
            .andExpect(jsonPath("$[1].deezerId").value(702));
    }

    @Test void searchArtists() throws Exception {
        when(deezer.searchArtists("stromae")).thenReturn(List.of(STROMAE));
        mvc.perform(get("/artists/search").param("q", "stromae"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Stromae"))
            .andExpect(jsonPath("$[0].pictureUrl").value("stromae.jpg"));
    }

    @Test void unknownTheme() throws Exception {
        mvc.perform(get("/themes/nope"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Thème nope introuvable"));
    }

    @Test void rejectShortQuery() throws Exception {
        mvc.perform(get("/artists/search").param("q", "s"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("La recherche doit contenir au moins 2 caractères"));
        mvc.perform(get("/tracks/search"))
            .andExpect(status().isBadRequest());
    }

    @Test void deezerDownGives503() throws Exception {
        when(deezer.searchArtists("stromae")).thenThrow(new DeezerUnavailable(new IllegalStateException()));
        mvc.perform(get("/artists/search").param("q", "stromae"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.detail").value("Deezer est indisponible pour le moment"));
    }

    private List<DeezerTrack> nine(long firstId) {
        return LongStream.rangeClosed(1, 9).mapToObj(i -> track(firstId + i, (int) i * 1000, "preview.mp3")).toList();
    }

    private DeezerTrack track(long id, int rank, String preview) {
        return new DeezerTrack(id, "Titre " + id, preview, rank, 200, STROMAE, ALBUM);
    }
}
