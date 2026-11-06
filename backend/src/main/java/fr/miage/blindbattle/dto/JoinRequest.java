package fr.miage.blindbattle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(
        @NotBlank(message = "Le pseudo est obligatoire") @Size(min = 2, max = 20, message = "Le pseudo fait entre 2 et 20 caractères") String nickname) {}
