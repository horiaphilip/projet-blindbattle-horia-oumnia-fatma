package fr.miage.blindbattle.dto;

import fr.miage.blindbattle.domain.GameSettings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record GameRequest(@Valid @NotNull(message = "Les réglages sont obligatoires") GameSettings settings) {}
