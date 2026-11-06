package fr.miage.blindbattle.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record GameSettings(
        @NotNull(message = "Le mode de réponse est obligatoire") @Enumerated(EnumType.STRING) AnswerMode answerMode,
        @NotNull(message = "La cible à deviner est obligatoire") @Enumerated(EnumType.STRING) GuessTarget guessTarget,
        @Min(value = 3, message = "3 manches minimum") @Max(value = 20, message = "20 manches maximum") int roundCount,
        @Min(value = 10, message = "10 secondes minimum") @Max(value = 30, message = "30 secondes maximum") int roundSeconds,
        @NotNull(message = "La difficulté est obligatoire") @Enumerated(EnumType.STRING) Difficulty difficulty,
        @NotBlank(message = "Le thème est obligatoire") String themeId) {}
