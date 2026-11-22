package fr.miage.blindbattle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AnswerRequest(
        @NotNull(message = "Le jeton du joueur est obligatoire") UUID playerToken,
        @Size(max = 200, message = "La réponse fait 200 caractères maximum") String text,
        @Min(value = 0, message = "Choix invalide") @Max(value = 3, message = "Choix invalide") Integer choiceIndex) {}
