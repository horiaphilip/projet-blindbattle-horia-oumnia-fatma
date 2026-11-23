package fr.miage.blindbattle.dto;

public record LeaderboardEntryView(long userId, String name, long gamesPlayed, long totalScore, int bestScore, long wins) {}
