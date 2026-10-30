package fr.miage.blindbattle.dto;

import java.util.List;

public record StatsView(long userId, int gamesPlayed, long totalScore, int bestScore, List<BadgeView> badges) {}
