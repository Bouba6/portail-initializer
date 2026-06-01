package sn.daef.portail.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminUserAccessRequest(

        @NotBlank(message = "Le code application est obligatoire")
        String appCode,

        @NotBlank(message = "Le rôle est obligatoire")
        String role
) {}
