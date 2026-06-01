package sn.daef.taxawu.security.context;

import java.util.List;
import java.util.UUID;

/**
 * Principal Taxawu — injecté dans le SecurityContext après validation.
 *
 * Récupérable dans n'importe quel controller via :
 *   AuthenticatedUser user = AuthenticatedUserHolder.current();
 * ou via @AuthenticationPrincipal si configuré.
 *
 * Immuable par nature (record Java 21).
 */
public record AuthenticatedUser(
        UUID userId,
        String email,
        String nomComplet,
        String roleSurTaxawu,   // rôle spécifique à cette application
        List<String> allRoles   // tous les rôles dans l'écosystème DAEF
) {
    public boolean isAdmin() {
        return "ADMIN".equals(roleSurTaxawu);
    }

    public boolean isEvaluateur() {
        return "EVALUATEUR".equals(roleSurTaxawu);
    }
}
