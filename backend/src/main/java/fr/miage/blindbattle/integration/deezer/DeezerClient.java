package fr.miage.blindbattle.integration.deezer;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class DeezerClient {
    private static final ParameterizedTypeReference<DeezerPage<DeezerTrack>> TRACKS = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<DeezerPage<DeezerArtist>> ARTISTS = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<DeezerTrack> TRACK = new ParameterizedTypeReference<>() {};
    private final RestClient client;

    public DeezerClient(RestClient.Builder builder, @Value("${blindbattle.deezer.url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Cacheable(cacheNames = "deezer-charts", unless = "#result.isEmpty()")
    public List<DeezerTrack> chartTracks(int limit) {
        return data(fetch(TRACKS, "/chart/0/tracks?limit={limit}", limit));
    }

    @Cacheable(cacheNames = "deezer-playlists", unless = "#result.isEmpty()")
    public List<DeezerTrack> playlistTracks(long playlistId) {
        return data(fetch(TRACKS, "/playlist/{id}/tracks?limit=100", playlistId));
    }

    @Cacheable(cacheNames = "deezer-artists", unless = "#result.isEmpty()")
    public List<DeezerArtist> searchArtists(String query) {
        return data(fetch(ARTISTS, "/search/artist?q={q}&limit=10", query));
    }

    @Cacheable(cacheNames = "deezer-artist-top", unless = "#result.isEmpty()")
    public List<DeezerTrack> artistTop(long artistId) {
        return data(fetch(TRACKS, "/artist/{id}/top?limit=50", artistId));
    }

    @Cacheable(cacheNames = "deezer-related", unless = "#result.isEmpty()")
    public List<DeezerArtist> relatedArtists(long artistId) {
        return data(fetch(ARTISTS, "/artist/{id}/related?limit=10", artistId));
    }

    @Cacheable(cacheNames = "deezer-search", unless = "#result.isEmpty()")
    public List<DeezerTrack> searchTracks(String query) {
        return data(fetch(TRACKS, "/search/track?q={q}&limit=25", query));
    }

    public DeezerTrack track(long trackId) {
        DeezerTrack track = fetch(TRACK, "/track/{id}", trackId);
        if (track == null || track.id() == null) throw new DeezerTrackNotFound(trackId);
        return track;
    }

    private <T> T fetch(ParameterizedTypeReference<T> type, String uri, Object... variables) {
        try {
            return client.get().uri(uri, variables).retrieve().body(type);
        } catch (RestClientException error) {
            throw new DeezerUnavailable(error);
        }
    }

    private <T> List<T> data(DeezerPage<T> page) {
        return page == null || page.data() == null ? List.of() : page.data();
    }
}
