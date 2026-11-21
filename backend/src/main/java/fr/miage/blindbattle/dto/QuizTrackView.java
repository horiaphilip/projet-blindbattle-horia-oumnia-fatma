package fr.miage.blindbattle.dto;

public record QuizTrackView(int position, long trackId, String title, String artist, String coverUrl, boolean hasPreview) {}
