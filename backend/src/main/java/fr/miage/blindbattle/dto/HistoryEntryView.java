package fr.miage.blindbattle.dto;

import java.time.Instant;

public record HistoryEntryView(String gameCode, Instant playedAt, int rank, long playerCount, int finalScore,
        int correctAnswers, int totalRounds) {}
