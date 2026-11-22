package fr.miage.blindbattle.dto;

import fr.miage.blindbattle.domain.QuizVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuizRequest(
        @NotBlank(message = "Le titre est obligatoire") @Size(max = 60, message = "Le titre fait 60 caractères maximum") String title,
        @Size(max = 300, message = "La description fait 300 caractères maximum") String description,
        @NotNull(message = "La visibilité est obligatoire") QuizVisibility visibility,
        @NotNull(message = "La liste des morceaux est obligatoire") @Size(max = 50, message = "Un quiz contient 50 morceaux maximum") List<@NotNull(message = "Identifiant de morceau manquant") Long> trackIds) {}
