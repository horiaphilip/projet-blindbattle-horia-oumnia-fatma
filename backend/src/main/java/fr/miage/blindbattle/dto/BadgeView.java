package fr.miage.blindbattle.dto;

import java.time.Instant;

public record BadgeView(String code, Instant earnedAt) {}
