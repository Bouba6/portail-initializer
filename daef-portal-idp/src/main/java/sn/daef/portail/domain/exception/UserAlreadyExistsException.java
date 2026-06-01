package sn.daef.portail.domain.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("Un compte existe déjà avec l'adresse : " + email);
    }
}
