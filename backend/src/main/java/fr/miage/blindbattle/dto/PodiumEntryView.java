package fr.miage.blindbattle.dto;

public record PodiumEntryView(int rank, String nickname, int finalScore, int correctAnswers, long avgResponseMs) {}
