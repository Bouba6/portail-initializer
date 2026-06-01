package sn.daef.portail.domain.port.input;

import sn.daef.portail.domain.model.TokenValidation;

/**
 * Input Port : valide un token JWT et retourne les droits associés.
 * Appelé par GET /api/auth/validate — consommé par Taxawu et Suquali via Feign.
 */
public interface ValidateTokenUseCase {
    TokenValidation execute(String bearerToken);
}
