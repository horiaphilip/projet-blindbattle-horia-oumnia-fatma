package fr.miage.blindbattle.dto;

import fr.miage.blindbattle.domain.GameSettings;
import java.time.Instant;
import java.util.List;

public record GameView(String code, String status, GameSettings settings, long hostId, String hostName,
        List<PlayerView> players, Instant createdAt) {}
