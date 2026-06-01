package sn.daef.taxawu.security.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * Réponse du endpoint de test /api/test/me.
 * Expose tout ce qui a été injecté dans le SecurityContext par le JwtAuthFilter
 * après validation auprès du Portail IDP.
 */
public record MeResponse(
        UUID userId,
        String email,
        String nomComplet,
        String roleSurTaxawu,
        List<String> tousLesRoles,
        boolean isAdmin,
        String message
) {}
