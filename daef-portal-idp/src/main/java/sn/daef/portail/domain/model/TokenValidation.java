package sn.daef.portail.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Value Object : résultat de la validation d'un token JWT.
 * Retourné par GET /api/auth/validate — utilisé par Taxawu et Suquali via Feign.
 */
public record TokenValidation(
        boolean valid,
        UUID userId,
        String email,
        String nomComplet,
        List<String> roles,       // rôles globaux
        List<AppAccess> appAccesses // droits par application
) {
    public static TokenValidation invalide() {
        return new TokenValidation(false, null, null, null, List.of(), List.of());
    }
}
