package sn.daef.taxawu.security.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import sn.daef.taxawu.security.exception.PortailUnavailableException;
import sn.daef.taxawu.security.exception.TokenRejectedException;

/**
 * Configuration du client Feign vers le Portail IDP.
 *
 * NE PAS mettre @Configuration ici — cette classe est passée directement
 * dans @FeignClient(configuration = ...) et ne doit PAS être dans le
 * component scan global, sinon elle s'appliquerait à tous les Feign clients.
 * C'est un pattern Feign standard et intentionnel.
 */
public class PortailFeignConfig {

    /**
     * Traduit les codes HTTP du Portail en exceptions métier Taxawu.
     * Principe : Taxawu ne connaît pas les détails internes du Portail,
     * il raisonne en termes de "token refusé" ou "portail indisponible".
     */
    @Bean
    public ErrorDecoder portailErrorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 401 -> new TokenRejectedException("Token invalide ou expiré");
            case 403 -> new TokenRejectedException("Accès interdit à Taxawu pour ce compte");
            case 502, 503, 504 -> new PortailUnavailableException(
                    "Le service d'authentification est temporairement indisponible");
            default -> new PortailUnavailableException(
                    "Erreur inattendue du portail : HTTP " + response.status());
        };
    }
}
