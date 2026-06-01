package sn.daef.portail.domain.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String reason) {
        super("Token invalide : " + reason);
    }
}
