package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.GuessTarget;
import fr.miage.blindbattle.domain.TrackEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;
import org.springframework.stereotype.Component;

@Component
public class DistractorPicker {
    private static final int CHOICE_COUNT = 4;

    public Choices choices(TrackEntity answer, List<TrackEntity> pool, GuessTarget target, RandomGenerator random) {
        List<TrackEntity> candidates = new ArrayList<>(pool);
        Collections.shuffle(candidates, random);
        List<TrackEntity> picked = new ArrayList<>(List.of(answer));
        for (TrackEntity candidate : candidates) {
            if (picked.size() == CHOICE_COUNT) break;
            if (differsFromAll(candidate, picked, target)) picked.add(candidate);
        }
        if (picked.size() < CHOICE_COUNT) throw new NotEnoughTracks();
        Collections.shuffle(picked, random);
        return new Choices(picked, picked.indexOf(answer));
    }

    public List<String> labels(Choices choices, GuessTarget target) {
        return choices.tracks().stream().map(track -> switch (target) {
            case TITLE -> track.getTitle();
            case ARTIST -> track.getArtist();
            case BOTH -> track.getTitle() + " – " + track.getArtist();
        }).toList();
    }

    private boolean differsFromAll(TrackEntity candidate, List<TrackEntity> picked, GuessTarget target) {
        return picked.stream().noneMatch(p -> p.getTitle().equalsIgnoreCase(candidate.getTitle())
                || (target != GuessTarget.TITLE && p.getArtist().equalsIgnoreCase(candidate.getArtist())));
    }
}
