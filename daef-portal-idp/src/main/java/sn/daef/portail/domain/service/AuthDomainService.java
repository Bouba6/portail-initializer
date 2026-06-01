package sn.daef.portail.domain.service;

import sn.daef.portail.domain.exception.AccountDisabledException;
import sn.daef.portail.domain.exception.InvalidCredentialsException;
import sn.daef.portail.domain.model.UserAccount;

/**
 * Domain Service : logique métier d'authentification qui ne peut pas
 * appartenir à un seul aggregate.
 * AUCUNE annotation Spring — testable sans contexte.
 */
public class AuthDomainService {

    private final PasswordEncoder passwordEncoder;

    public AuthDomainService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Vérifie que le compte peut se connecter et que le mot de passe correspond.
     * Retourne le UserAccount si tout est OK, lève une exception sinon.
     */
    public UserAccount authentifier(UserAccount account, String rawPassword) {
        if (!account.peutSeConnecter()) {
            throw new AccountDisabledException(account.getEmail());
        }
        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return account;
    }

    /**
     * Port secondaire interne : abstraction du BCrypt pour que le domaine
     * reste indépendant de Spring Security.
     */
    public interface PasswordEncoder {
        String encode(String rawPassword);
        boolean matches(String rawPassword, String encodedPassword);
    }
}
