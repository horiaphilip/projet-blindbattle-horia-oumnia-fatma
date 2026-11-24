package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.integration.deezer.DeezerTrack;
import fr.miage.blindbattle.integration.deezer.DeezerTrackTranslator;
import fr.miage.blindbattle.repository.TrackRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackService {
    private final TrackRepository repository;
    private final DeezerTrackTranslator translator;

    public TrackService(TrackRepository repository, DeezerTrackTranslator translator) {
        this.repository = repository;
        this.translator = translator;
    }

    @Transactional
    public List<TrackEntity> saveAll(List<DeezerTrack> tracks) { return tracks.stream().map(this::save).toList(); }

    private TrackEntity save(DeezerTrack track) {
        return repository.findByDeezerId(track.id()).map(existing -> refreshed(existing, track))
                .orElseGet(() -> repository.save(translator.toEntity(track)));
    }

    private TrackEntity refreshed(TrackEntity entity, DeezerTrack track) {
        translator.refresh(entity, track);
        return entity;
    }
}
