package fr.miage.blindbattle.dto;

public record TrackView(long id, long deezerId, String title, String artist, String album, String coverUrl, int rank,
        int durationSeconds, boolean hasPreview) {}
