package sn.daef.portail.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import sn.daef.portail.domain.port.output.TokenBlacklistRepository;

import java.time.Duration;

/**
 * Adapteur Redis pour la blacklist de tokens révoqués.
 * Pattern : SET blacklist:{token} "revoked" EX {ttlSeconds}
 * Un token révoqué reste en Redis jusqu'à son expiration naturelle.
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistRepositoryAdapter implements TokenBlacklistRepository {

    private static final String PREFIX = "blacklist:";
    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklist(String token, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                PREFIX + token,
                "revoked",
                Duration.ofSeconds(ttlSeconds)
        );
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}
