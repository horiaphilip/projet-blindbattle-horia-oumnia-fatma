package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.Difficulty;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.dto.ArtistView;
import fr.miage.blindbattle.dto.ThemeDetailView;
import fr.miage.blindbattle.dto.ThemeView;
import fr.miage.blindbattle.dto.TrackView;
import fr.miage.blindbattle.integration.deezer.DeezerArtist;
import fr.miage.blindbattle.integration.deezer.DeezerClient;
import fr.miage.blindbattle.integration.deezer.DeezerTrack;
import fr.miage.blindbattle.integration.deezer.DeezerUnavailable;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogueService {
    private static final int SEARCH_MIN_LENGTH = 2;

    private final ThemeCatalog catalog;
    private final DeezerClient deezer;
    private final TrackService tracks;

    public CatalogueService(ThemeCatalog catalog, DeezerClient deezer, TrackService tracks) {
        this.catalog = catalog;
        this.deezer = deezer;
        this.tracks = tracks;
    }

    public List<ThemeView> themes() {
        return catalog.all().stream().map(theme -> view(theme, coverOf(theme))).toList();
    }

    @Transactional
    public ThemeDetailView theme(String id, Difficulty difficulty) {
        List<DeezerTrack> source = deezerTracks(id);
        List<TrackView> pool = byDifficulty(tracks.saveAll(source), difficulty).stream().map(this::view).toList();
        return new ThemeDetailView(themeView(id, source), difficulty.name(), pool);
    }

    @Transactional
    public List<TrackEntity> pool(String themeId, Difficulty difficulty) {
        return byDifficulty(tracks.saveAll(deezerTracks(themeId)), difficulty);
    }

    public List<ArtistView> searchArtists(String query) {
        return deezer.searchArtists(checked(query)).stream().map(this::view).toList();
    }

    @Transactional
    public List<TrackView> artistTop(long artistId) {
        return tracks.saveAll(deezer.artistTop(artistId)).stream().map(this::view).toList();
    }

    public List<ArtistView> related(long artistId) {
        return deezer.relatedArtists(artistId).stream().map(this::view).toList();
    }

    @Transactional
    public List<TrackView> searchTracks(String query) {
        return tracks.saveAll(deezer.searchTracks(checked(query))).stream().map(this::view).toList();
    }

    public String freshPreview(long deezerId) { return deezer.track(deezerId).preview(); }

    List<TrackEntity> byDifficulty(List<TrackEntity> candidates, Difficulty difficulty) {
        List<TrackEntity> playable = candidates.stream().filter(TrackEntity::hasPreview)
                .sorted(Comparator.comparingInt(TrackEntity::getRank).reversed()).toList();
        int popular = (playable.size() + 2) / 3;
        return difficulty == Difficulty.EASY ? playable.subList(0, popular) : playable.subList(popular, playable.size());
    }

    private List<DeezerTrack> deezerTracks(String themeId) {
        if (ThemeCatalog.WORLD_CHART.equals(themeId)) return deezer.chartTracks(100);
        if (themeId.startsWith(ThemeCatalog.ARTIST_PREFIX)) return deezer.artistTop(artistId(themeId));
        Theme theme = catalog.find(themeId).orElseThrow(() -> new ThemeNotFound(themeId));
        return deezer.playlistTracks(theme.playlistId());
    }

    private ThemeView themeView(String id, List<DeezerTrack> source) {
        String cover = source.isEmpty() ? null : cover(source.getFirst());
        if (!id.startsWith(ThemeCatalog.ARTIST_PREFIX)) {
            return view(catalog.find(id).orElseThrow(() -> new ThemeNotFound(id)), cover);
        }
        return new ThemeView(id, "100% " + artistName(source), "Artiste", cover);
    }

    private String artistName(List<DeezerTrack> source) {
        if (source.isEmpty() || source.getFirst().artist() == null) return "Artiste inconnu";
        return source.getFirst().artist().name();
    }

    private long artistId(String themeId) {
        try {
            return Long.parseLong(themeId.substring(ThemeCatalog.ARTIST_PREFIX.length()));
        } catch (NumberFormatException error) {
            throw new ThemeNotFound(themeId);
        }
    }

    private String coverOf(Theme theme) {
        try {
            List<DeezerTrack> source = deezerTracks(theme.id());
            return source.isEmpty() ? null : cover(source.getFirst());
        } catch (DeezerUnavailable error) {
            return null;
        }
    }

    private String cover(DeezerTrack track) { return track.album() == null ? null : track.album().cover(); }

    private String checked(String query) {
        if (query == null || query.strip().length() < SEARCH_MIN_LENGTH) throw new SearchTooShort();
        return query.strip();
    }

    private ThemeView view(Theme theme, String cover) {
        return new ThemeView(theme.id(), theme.name(), theme.category(), cover);
    }

    private ArtistView view(DeezerArtist artist) { return new ArtistView(artist.id(), artist.name(), artist.picture()); }

    private TrackView view(TrackEntity track) {
        return new TrackView(track.getId(), track.getDeezerId(), track.getTitle(), track.getArtist(), track.getAlbum(),
                track.getCoverUrl(), track.getRank(), track.getDurationSeconds(), track.hasPreview());
    }
}
