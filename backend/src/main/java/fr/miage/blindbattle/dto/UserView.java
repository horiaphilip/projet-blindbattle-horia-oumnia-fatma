package fr.miage.blindbattle.dto;

import java.time.Instant;

public record UserView(long id, String name, String email, Instant createdAt, Instant updatedAt) {}
