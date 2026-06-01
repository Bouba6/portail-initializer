package sn.daef.taxawu.security.context;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilitaire statique pour récupérer l'utilisateur courant depuis n'importe où.
 *
 * Usage dans un controller Taxawu :
 *   AuthenticatedUser me = AuthenticatedUserHolder.current();
 *   log.info("Requête de {} ({})", me.nomComplet(), me.roleSurTaxawu());
 *
 * Lance IllegalStateException si appelé hors d'un contexte authentifié.
 */
public final class AuthenticatedUserHolder {

    private AuthenticatedUserHolder() {}

    public static AuthenticatedUser current() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser)) {
            throw new IllegalStateException(
                    "Aucun utilisateur authentifié dans le contexte courant");
        }
        return (AuthenticatedUser) auth.getPrincipal();
    }
}
