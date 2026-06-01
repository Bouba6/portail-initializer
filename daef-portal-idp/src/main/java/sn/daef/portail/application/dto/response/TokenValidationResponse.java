package sn.daef.portail.application.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Retourné par GET /api/auth/validate
 * Consommé par Taxawu et Suquali via OpenFeign pour valider chaque requête.
 */
public record TokenValidationResponse(
        boolean valid,
        UUID userId,
        String email,
        String nomComplet,
        List<String> roles,
        List<AppAccessResponse> applications
) {}
