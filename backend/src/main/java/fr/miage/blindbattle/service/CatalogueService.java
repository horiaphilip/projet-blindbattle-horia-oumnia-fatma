package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.Difficulty;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.repository.TrackRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogueService {
    private final TrackRepository tracks;

    public CatalogueService(TrackRepository tracks) { this.tracks = tracks; }

    @Transactional(readOnly = true)
    public List<TrackEntity> pool(String themeId, Difficulty difficulty) { return byDifficulty(tracks.findAll(), difficulty); }

    List<TrackEntity> byDifficulty(List<TrackEntity> candidates, Difficulty difficulty) {
        List<TrackEntity> playable = candidates.stream().filter(TrackEntity::hasPreview)
                .sorted(Comparator.comparingInt(TrackEntity::getRank).reversed()).toList();
        int popular = (playable.size() + 2) / 3;
        return difficulty == Difficulty.EASY ? playable.subList(0, popular) : playable.subList(popular, playable.size());
    }
}
