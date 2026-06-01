package sn.daef.taxawu.security.exception;

/**
 * Levée quand le Portail IDP est inaccessible (timeout, 502, 503, 504).
 * Le filtre renvoie alors un 503 à l'appelant — Taxawu est dégradé,
 * pas compromis. Distinction importante avec TokenRejectedException.
 */
public class PortailUnavailableException extends RuntimeException {

    public PortailUnavailableException(String message) {
        super(message);
    }
}
