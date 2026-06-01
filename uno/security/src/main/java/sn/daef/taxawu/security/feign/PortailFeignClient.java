package sn.daef.taxawu.security.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Client Feign vers le Portail IDP.
 *
 * ${app.portail.base-url} est configuré dans application.properties.
 * En local : http://localhost:8080
 * En Docker : http://portail-service:8080
 *
 * Configuration Feign séparée (PortailFeignConfig) pour isoler
 * l'ErrorDecoder sans affecter d'autres clients Feign potentiels.
 */
@FeignClient(
        name = "daef-portal-idp",
        url = "${app.portail.base-url}",
        configuration = PortailFeignConfig.class
)
public interface PortailFeignClient {

    /**
     * Valide un token JWT et retourne les droits associés.
     * Appelé à CHAQUE requête entrante sur Taxawu — doit être < 20ms P99.
     * Configurer un timeout Feign agressif en conséquence.
     */
    @GetMapping("/api/auth/validate")
    PortailTokenValidationDto validateToken(
            @RequestHeader("Authorization") String bearerToken);
}
