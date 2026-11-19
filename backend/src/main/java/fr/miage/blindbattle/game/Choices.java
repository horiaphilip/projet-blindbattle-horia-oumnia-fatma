package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.TrackEntity;
import java.util.List;

public record Choices(List<TrackEntity> tracks, int correctIndex) {}
