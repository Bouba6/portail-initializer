package sn.daef.portail.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.daef.portail.domain.model.AppAccess;
import sn.daef.portail.domain.model.TokenValidation;
import sn.daef.portail.domain.port.input.ValidateTokenUseCase;
import sn.daef.portail.domain.port.output.AppAccessRepository;
import sn.daef.portail.domain.port.output.TokenBlacklistRepository;
import sn.daef.portail.domain.port.output.UserAccountRepository;
import sn.daef.portail.infrastructure.security.jwt.JwtService;

import java.util.List;
import java.util.UUID;

/**
 * Use Case critique : appelé par Taxawu et Suquali à CHAQUE requête via Feign.
 * Doit être rapide — Redis en premier pour la blacklist, puis JWT parsing.
 */
@Service
@RequiredArgsConstructor
public class ValidateTokenUseCaseImpl implements ValidateTokenUseCase {

    private final JwtService jwtService;
    private final TokenBlacklistRepository blacklistRepository;
    private final UserAccountRepository userAccountRepository;
    private final AppAccessRepository appAccessRepository;

    @Override
    public TokenValidation execute(String bearerToken) {
        String token = extractToken(bearerToken);

        // 1. Blacklist check (Redis — ultra rapide)
        if (blacklistRepository.isBlacklisted(token)) {
            return TokenValidation.invalide();
        }

        // 2. Signature & expiration JWT
        if (!jwtService.isTokenValid(token)) {
            return TokenValidation.invalide();
        }

        // 3. Extraction des claims
        UUID userId = jwtService.extractUserId(token);
        String email = jwtService.extractEmail(token);

        // 4. Compte toujours actif en base ?
        var account = userAccountRepository.findById(userId);
        if (account.isEmpty() || !account.get().isActif()) {
            return TokenValidation.invalide();
        }

        // 5. Accès aux applications (peut venir du cache Redis si tu actives @Cacheable)
        List<AppAccess> accesses = appAccessRepository.findByUserId(userId);

        return new TokenValidation(
                true,
                userId,
                email,
                account.get().getNomComplet(),
                List.of(jwtService.extractRole(token)),
                accesses
        );
    }

    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
}
