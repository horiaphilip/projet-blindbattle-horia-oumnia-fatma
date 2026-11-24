package fr.miage.blindbattle.integration.deezer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DeezerClientTest {
    private static final String URL = "https://api.deezer.com";
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final DeezerClient client = new DeezerClient(builder, URL);

    @AfterEach void verify() { server.verify(); }

    @Test void chartTracks() throws IOException {
        respond("/chart/0/tracks?limit=3", "chart");
        assertThat(client.chartTracks(3)).hasSize(3).first().satisfies(track -> {
            assertThat(track.title()).isEqualTo("Alors on danse");
            assertThat(track.artist().name()).isEqualTo("Stromae");
            assertThat(track.album().cover()).contains("250x250");
            assertThat(track.rank()).isEqualTo(912345);
        });
    }

    @Test void playlistTracks() throws IOException {
        respond("/playlist/1109890291/tracks?limit=100", "playlist");
        assertThat(client.playlistTracks(1109890291L)).extracting(DeezerTrack::id).containsExactly(3135556L, 1109731L);
    }

    @Test void searchArtists() throws IOException {
        respond("/search/artist?q=stromae&limit=10", "artist-search");
        assertThat(client.searchArtists("stromae")).first().satisfies(artist -> {
            assertThat(artist.id()).isEqualTo(5542);
            assertThat(artist.picture()).contains("250x250");
        });
    }

    @Test void searchEncodesTheQuery() throws IOException {
        respond("/search/artist?q=daft%20punk%26co&limit=10", "artist-search");
        assertThat(client.searchArtists("daft punk&co")).hasSize(2);
    }

    @Test void artistTop() throws IOException {
        respond("/artist/5542/top?limit=50", "artist-top");
        assertThat(client.artistTop(5542)).extracting(DeezerTrack::title).containsExactly("Santé", "Alors on danse");
    }

    @Test void relatedArtists() throws IOException {
        respond("/artist/5542/related?limit=10", "related");
        assertThat(client.relatedArtists(5542)).extracting(DeezerArtist::name).containsExactly("Angèle", "Orelsan");
    }

    @Test void searchTracks() throws IOException {
        respond("/search/track?q=stromae&limit=25", "track-search");
        assertThat(client.searchTracks("stromae")).extracting(DeezerTrack::preview).last().isEqualTo("");
    }

    @Test void track() throws IOException {
        respond("/track/3135556", "track");
        assertThat(client.track(3135556).preview()).startsWith("https://");
    }

    @Test void errorGivesEmptyList() throws IOException {
        respond("/search/track?q=zzzzzz&limit=25", "error");
        assertThat(client.searchTracks("zzzzzz")).isEmpty();
    }

    @Test void unknownTrack() throws IOException {
        respond("/track/1", "error");
        assertThatThrownBy(() -> client.track(1)).isInstanceOf(DeezerTrackNotFound.class).hasMessageContaining("1");
    }

    @Test void serverErrorGivesUnavailable() {
        server.expect(requestTo(URL + "/chart/0/tracks?limit=3")).andRespond(withServerError());
        assertThatThrownBy(() -> client.chartTracks(3)).isInstanceOf(DeezerUnavailable.class)
                .hasMessage("Deezer est indisponible pour le moment");
    }

    private void respond(String path, String fixture) throws IOException {
        String body = new ClassPathResource("deezer/" + fixture + ".json").getContentAsString(StandardCharsets.UTF_8);
        server.expect(requestTo(URL + path)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }
}
