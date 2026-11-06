package fr.miage.blindbattle.dto;

import java.time.Instant;

public record GameStateView(String code, String status, int currentRound, int totalRounds, Instant endsAt,
        Instant serverTime, long playerCount) {}
