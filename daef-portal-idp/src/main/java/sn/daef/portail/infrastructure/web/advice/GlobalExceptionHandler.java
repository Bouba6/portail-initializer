package sn.daef.portail.infrastructure.web.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sn.daef.portail.domain.exception.*;

import java.net.URI;
import java.time.Instant;

/**
 * Gestion centralisée des erreurs — ProblemDetail RFC 7807.
 * Même format JSON pour tous les services DAEF → cohérence front.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildProblem(HttpStatus.CONFLICT, ex.getMessage(), "user-already-exists");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, ex.getMessage(), "user-not-found");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildProblem(HttpStatus.UNAUTHORIZED, ex.getMessage(), "invalid-credentials");
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ProblemDetail handleAccountDisabled(AccountDisabledException ex) {
        return buildProblem(HttpStatus.FORBIDDEN, ex.getMessage(), "account-disabled");
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException ex) {
        return buildProblem(HttpStatus.UNAUTHORIZED, ex.getMessage(), "invalid-token");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream().map(e -> e.getField() + " : " + e.getDefaultMessage())
                .reduce((a, b) -> a + " | " + b).orElse("Erreur de validation");
        return buildProblem(HttpStatus.BAD_REQUEST, message, "validation-error");
    }

    private ProblemDetail buildProblem(HttpStatus status, String detail, String errorCode) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://daef.sn/errors/" + errorCode));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", errorCode);
        return problem;
    }
}
