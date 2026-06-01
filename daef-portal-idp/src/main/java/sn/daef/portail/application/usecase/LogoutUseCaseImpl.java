package sn.daef.portail.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sn.daef.portail.domain.port.input.LogoutUseCase;
import sn.daef.portail.domain.port.output.TokenBlacklistRepository;
import sn.daef.portail.infrastructure.security.jwt.JwtService;

@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final JwtService jwtService;
    private final TokenBlacklistRepository blacklistRepository;

    @Override
    public void execute(String bearerToken) {
        String token = bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7) : bearerToken;

        long ttl = jwtService.getRemainingTtlSeconds(token);
        if (ttl > 0) {
            blacklistRepository.blacklist(token, ttl);
        }
    }
}
