package fr.miage.blindbattle.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Le nom est obligatoire") @Size(max = 40, message = "Le nom fait 40 caractères maximum") String name,
        @NotBlank(message = "L'email est obligatoire") @Email(message = "L'email n'est pas valide") String email) {}
