package sn.daef.taxawu.security.exception;

/**
 * Levée quand le Portail IDP retourne 401 ou 403.
 * Signifie que le token est invalide, expiré, révoqué,
 * ou que ce compte n'a pas accès à Taxawu.
 */
public class TokenRejectedException extends RuntimeException {

    public TokenRejectedException(String message) {
        super(message);
    }
}
