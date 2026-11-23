package fr.miage.blindbattle.messaging;

import java.util.UUID;

public record PlayerResult(UUID eventId, String gameCode, long gameId, long playerId, Long userId, String nickname,
        int rank, int playerCount, int finalScore, int correctAnswers, long avgResponseMs) {}
