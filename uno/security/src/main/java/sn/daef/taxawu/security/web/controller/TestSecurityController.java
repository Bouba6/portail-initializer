package sn.daef.taxawu.security.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.daef.taxawu.security.context.AuthenticatedUser;
import sn.daef.taxawu.security.web.dto.MeResponse;

import java.util.Map;

/**
 * Controller de test de sécurité — à utiliser pendant le développement
 * pour vérifier que le flux JWT Portail → Taxawu fonctionne end-to-end.
 *
 * Scénarios couverts :
 *  GET /api/test/me          → tout utilisateur authentifié sur Taxawu
 *  GET /api/test/admin-only  → uniquement les ADMIN
 *  GET /api/test/evaluateur  → EVALUATEUR ou ADMIN
 *  GET /api/test/public      → sans token (vérifier que le filtre laisse passer)
 *
 * Supprimer ou protéger ce controller en production.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tests Sécurité", description = "Endpoints de vérification du flux SSO Taxawu ↔ Portail IDP")
@SecurityRequirement(name = "Bearer Token")
public class TestSecurityController {

    /**
     * Endpoint principal : retourne tout ce que le JwtAuthFilter a injecté
     * dans le SecurityContext après validation auprès du Portail IDP.
     *
     * Comment tester :
     *   1. POST /api/auth/login sur le Portail → récupérer le token
     *   2. GET /api/test/me avec Authorization: Bearer <token>
     *   3. Vérifier que userId, email, roleSurTaxawu sont corrects
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Qui suis-je ? — Dump complet du SecurityContext Taxawu")
    public MeResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        log.info("[TEST] /me appelé par {} ({})", user.email(), user.roleSurTaxawu());

        return new MeResponse(
                user.userId(),
                user.email(),
                user.nomComplet(),
                user.roleSurTaxawu(),
                user.allRoles(),
                user.isAdmin(),
                "✅ Token validé par le Portail IDP — vous êtes bien authentifié sur Taxawu"
        );
    }

    /**
     * Réservé aux administrateurs.
     * Retourne 403 si le rôle TAXAWU du compte n'est pas ADMIN.
     *
     * Tester avec un compte admin@daef.sn (créé par V2__seed_applications.sql).
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Zone ADMIN — 403 si vous n'êtes pas admin Taxawu")
    public Map<String, Object> adminOnly(@AuthenticationPrincipal AuthenticatedUser user) {
        log.info("[TEST] /admin-only accédé par {} — ADMIN confirmé", user.email());

        return Map.of(
                "status", "✅ Accès ADMIN accordé",
                "userId", user.userId(),
                "email", user.email(),
                "message", "Vous avez le rôle ADMIN sur Taxawu — vous pouvez gérer les dossiers"
        );
    }

    /**
     * Accessible aux évaluateurs ET aux admins.
     * Simule un endpoint métier Taxawu typique (consultation de dossier).
     */
    @GetMapping("/evaluateur")
    @PreAuthorize("hasAnyRole('EVALUATEUR', 'ADMIN')")
    @Operation(summary = "Zone EVALUATEUR — accessible EVALUATEUR et ADMIN")
    public Map<String, Object> evaluateurZone(@AuthenticationPrincipal AuthenticatedUser user) {
        log.info("[TEST] /evaluateur accédé par {} ({})", user.email(), user.roleSurTaxawu());

        return Map.of(
                "status", "✅ Accès accordé",
                "role", user.roleSurTaxawu(),
                "userId", user.userId(),
                "message", "Accès EVALUATEUR/ADMIN confirmé — vous pouvez consulter les dossiers FNCF"
        );
    }

    /**
     * Accessible aux PREFET uniquement.
     * Simule une zone de supervision territoriale.
     */
    @GetMapping("/prefet")
    @PreAuthorize("hasRole('PREFET')")
    @Operation(summary = "Zone PREFET — réservée aux préfets de région")
    public Map<String, Object> prefetZone(@AuthenticationPrincipal AuthenticatedUser user) {
        return Map.of(
                "status", "✅ Accès PREFET accordé",
                "userId", user.userId(),
                "region", "À définir selon le profil",
                "message", "Zone de supervision territoriale DAEF"
        );
    }

    /**
     * Endpoint public — sans token.
     * Permet de vérifier que SecurityConfig laisse bien passer
     * les requêtes sans Authorization header (Spring renverra 403 plus tard
     * sur les endpoints protégés, mais ne bloque pas ici).
     *
     * NOTE : configurer ce path dans SecurityConfig.requestMatchers(...).permitAll()
     * si vous voulez qu'il soit vraiment public sans authentification.
     */
    @GetMapping("/public")
    @Operation(summary = "Endpoint public — test sans token")
    public Map<String, String> publicEndpoint() {
        return Map.of(
                "status", "✅ Endpoint public accessible",
                "message", "Ce endpoint ne nécessite pas de token JWT",
                "hint", "Si vous voyez ceci sans token, le filtre laisse bien passer les requêtes anonymes"
        );
    }
}
