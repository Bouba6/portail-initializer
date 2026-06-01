package sn.daef.taxawu.security.feign;

import java.util.List;
import java.util.UUID;

/**
 * DTO miroir du TokenValidationResponse du Portail IDP.
 * Ce record est le contrat de désérialisation de la réponse Feign.
 *
 * IMPORTANT : les noms des champs doivent correspondre exactement
 * aux noms JSON retournés par GET /api/auth/validate du Portail.
 */
public record PortailTokenValidationDto(
        boolean valid,
        UUID userId,
        String email,
        String nomComplet,
        List<String> roles,
        List<AppAccessDto> applications
) {

    public record AppAccessDto(
            String appCode,
            String appName,
            String appBaseUrl,
            String appIconPath,
            String role
    ) {}

    /**
     * Retourne le rôle spécifique à Taxawu, ou null si ce user n'a pas accès.
     * C'est ici que se fait le contrôle d'accès applicatif :
     * un token valide ne suffit pas — il faut aussi être dans la liste TAXAWU.
     */
    public String rolePourTaxawu() {
        if (applications == null) return null;
        return applications.stream()
                .filter(a -> "TAXAWU".equals(a.appCode()))
                .map(AppAccessDto::role)
                .findFirst()
                .orElse(null);
    }
}
