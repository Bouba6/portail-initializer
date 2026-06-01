package sn.daef.portail.domain.port.output;

/**
 * Output Port : gestion de la blacklist de tokens révoqués (Redis).
 * TTL = durée restante d'expiration du token.
 */
public interface TokenBlacklistRepository {
    void blacklist(String token, long ttlSeconds);
    boolean isBlacklisted(String token);
}
