package sn.daef.portail.domain.exception;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException(String email) {
        super("Le compte " + email + " est désactivé. Contactez l'administrateur DAEF.");
    }
}
