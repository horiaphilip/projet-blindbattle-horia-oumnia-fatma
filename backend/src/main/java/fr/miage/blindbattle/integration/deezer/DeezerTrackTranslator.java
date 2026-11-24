package fr.miage.blindbattle.integration.deezer;

import fr.miage.blindbattle.domain.TrackEntity;
import org.springframework.stereotype.Component;

@Component
public class DeezerTrackTranslator {
    public TrackEntity toEntity(DeezerTrack track) {
        return new TrackEntity(track.id(), track.title(), artistName(track), artistId(track), albumTitle(track),
                cover(track), rank(track), duration(track), hasPreview(track));
    }

    public void refresh(TrackEntity entity, DeezerTrack track) {
        entity.refresh(track.title(), artistName(track), artistId(track), albumTitle(track), cover(track), rank(track),
                duration(track), hasPreview(track));
    }

    private int rank(DeezerTrack track) { return track.rank() == null ? 0 : track.rank(); }

    private int duration(DeezerTrack track) { return track.duration() == null ? 0 : track.duration(); }

    private boolean hasPreview(DeezerTrack track) { return track.preview() != null && !track.preview().isBlank(); }

    private String artistName(DeezerTrack track) {
        return track.artist() == null ? "Artiste inconnu" : track.artist().name();
    }

    private long artistId(DeezerTrack track) { return track.artist() == null ? 0 : track.artist().id(); }

    private String albumTitle(DeezerTrack track) { return track.album() == null ? null : track.album().title(); }

    private String cover(DeezerTrack track) { return track.album() == null ? null : track.album().cover(); }
}
