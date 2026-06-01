package sn.daef.portail.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Le nom complet est obligatoire")
        String nomComplet,

        @Email(message = "Format email invalide")
        @NotBlank(message = "L'email est obligatoire")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Minimum 8 caractères")
        String password,

        String telephone,

        @NotBlank(message = "Le code application est obligatoire")
        String appCode,   // ex: "TAXAWU"

        @NotBlank(message = "Le rôle est obligatoire")
        String role       // ex: "ADMIN", "EVALUATEUR"
) {}
