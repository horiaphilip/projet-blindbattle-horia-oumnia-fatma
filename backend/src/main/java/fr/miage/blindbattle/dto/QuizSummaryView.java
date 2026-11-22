package fr.miage.blindbattle.dto;

import java.time.Instant;

public record QuizSummaryView(long id, String title, String visibility, int trackCount, Instant createdAt) {}
