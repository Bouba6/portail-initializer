package sn.daef.portail.infrastructure.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sn.daef.portail.domain.service.AuthDomainService;
import sn.daef.portail.infrastructure.security.filter.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/validate",   // appelé par Feign depuis Taxawu/Suquali
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Tout le reste nécessite authentification
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * BCryptPasswordEncoder exposé en Bean ET implémentant le port domaine.
     * Cela évite que le domaine dépende de Spring Security.
     */
    @Bean
    public AuthDomainService.PasswordEncoder domainPasswordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new AuthDomainService.PasswordEncoder() {
            @Override
            public String encode(String rawPassword) {
                return bcrypt.encode(rawPassword);
            }
            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return bcrypt.matches(rawPassword, encodedPassword);
            }
        };
    }

    @Bean
    public AuthDomainService authDomainService(AuthDomainService.PasswordEncoder encoder) {
        return new AuthDomainService(encoder);
    }
}
