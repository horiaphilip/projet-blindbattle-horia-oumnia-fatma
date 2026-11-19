package fr.miage.blindbattle.game;

import fr.miage.blindbattle.domain.GuessTarget;

public record AnswerAttempt(String text, Integer choiceIndex, String expectedTitle, String expectedArtist,
        int correctChoiceIndex, GuessTarget target) {}
