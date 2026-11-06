package fr.miage.blindbattle.dto;

import java.util.UUID;

public record JoinView(long playerId, String nickname, UUID playerToken) {}
