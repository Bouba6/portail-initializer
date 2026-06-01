package sn.daef.portail.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration Redis.
 * On utilise StringRedisTemplate (pas RedisTemplate générique) car
 * toutes nos valeurs sont des String simples (tokens, flags "revoked").
 * Cela évite les problèmes de sérialisation Java et garde Redis lisible
 * avec redis-cli pour le debug en production.
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}


// sed -i 's|<!-- ── Tests|<!-- ── Flyway (migrations SQL) ──────────────────── -->\n        <dependency>\n            <groupId>org.flywaydb</groupId>\n            <artifactId>flyway-core</artifactId>\n        </dependency>\n        <dependency>\n            <groupId>org.flywaydb</groupId>\n            <artifactId>flyway-database-postgresql</artifactId>\n        </dependency>\n\n        <!-- ── Tests|' /home/claude/daef-portal-idp/pom.xml

// # Ajouter config Flyway dans application.yml
// cat >> /Users/admin/Downloads/initializer/daef-portal-idp/src/main/resources/application.yml << 'YAML'

//   flyway:
//     enabled: true
//     locations: classpath:db/migration
//     baseline-on-migrate: true
//     validate-on-migrate: true
// YAML

// echo "✅ Flyway ajouté au pom.xml et application.yml"