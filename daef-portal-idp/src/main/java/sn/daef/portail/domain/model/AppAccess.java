package sn.daef.portail.domain.model;

/**
 * Value Object : représente le droit d'accès d'un utilisateur
 * à une application de l'écosystème DAEF.
 * Immuable par nature (record Java 21).
 *
 * Exemples :
 *   appCode = "TAXAWU",  role = "EVALUATEUR"
 *   appCode = "SUQUALI", role = "ADMIN"
 */
public record AppAccess(
        String appCode,
        String appName,
        String appBaseUrl,
        String appIconPath,
        String role
) {}
