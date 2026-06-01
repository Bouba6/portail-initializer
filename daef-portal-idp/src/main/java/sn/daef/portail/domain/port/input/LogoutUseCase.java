package sn.daef.portail.domain.port.input;

/**
 * Input Port : révoque un token JWT en le mettant en blacklist Redis.
 * Garantit qu'un agent DAEF révoqué ne peut plus accéder à Taxawu/Suquali
 * même si son token n'est pas encore expiré.
 */
public interface LogoutUseCase {
    void execute(String bearerToken);
}
