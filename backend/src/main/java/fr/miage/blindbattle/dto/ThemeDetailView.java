package fr.miage.blindbattle.dto;

import java.util.List;

public record ThemeDetailView(ThemeView theme, String difficulty, List<TrackView> tracks) {}
