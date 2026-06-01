package sn.daef.portail.application.dto.response;

/**
 * Retourné par GET /api/users/{id}/apps
 * Utilisé par le frontend Launchpad pour afficher les icônes d'applications.
 */
public record AppAccessResponse(
        String appCode,
        String appName,
        String appBaseUrl,
        String appIconPath,
        String role
) {}
