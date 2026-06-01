package sn.daef.taxawu.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sn.daef.taxawu.security.feign.PortailFeignClient;
import sn.daef.taxawu.security.filter.JwtAuthFilter;

/**
 * Configuration Spring Security du module Taxawu.
 *
 * Ce fichier était ENTIÈREMENT MANQUANT — sans lui, Spring Boot auto-configure
 * une Basic Auth par défaut, ce qui est incompatible avec le modèle Feign.
 *
 * @EnableMethodSecurity active @PreAuthorize/@PostAuthorize dans les controllers
 * pour du contrôle d'accès fin (ex: @PreAuthorize("hasRole('ADMIN')")).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final PortailFeignClient portailFeignClient;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(portailFeignClient);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints de santé toujours accessibles
                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Tout le reste nécessite un token valide validé par le Portail
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
