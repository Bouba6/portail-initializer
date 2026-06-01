package sn.daef.taxawu.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.daef.taxawu.security.context.AuthenticatedUser;
import sn.daef.taxawu.security.exception.PortailUnavailableException;
import sn.daef.taxawu.security.exception.TokenRejectedException;
import sn.daef.taxawu.security.feign.PortailFeignClient;
import sn.daef.taxawu.security.feign.PortailTokenValidationDto;

import java.io.IOException;
import java.util.List;

/**
 * Filtre de sécurité Taxawu — délègue 100% de la validation JWT au Portail IDP.
 *
 * Flux (identique au diagramme de séquence) :
 * 1. Extraction du header Authorization
 * 2. Appel Feign → Portail IDP GET /api/auth/validate
 * 3. Si valid=true ET accès TAXAWU → construction AuthenticatedUser + SecurityContext
 * 4. Si valid=false → 401 propre sans stack trace
 * 5. Si portail KO → 503 (Taxawu dégradé, pas compromis)
 *
 * Ce filtre ne connaît PAS les secrets JWT. Règle d'or de l'architecture centralisée.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final PortailFeignClient portailFeignClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Pas de token → on laisse passer (Spring Security gèrera via SecurityConfig)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            PortailTokenValidationDto validation =
                    portailFeignClient.validateToken(authHeader);

            if (!validation.valid()) {
                writeUnauthorized(response, "Token invalide ou révoqué");
                return;
            }

            // Vérification que ce user a bien accès à TAXAWU spécifiquement
            String roleTaxawu = validation.rolePourTaxawu();
            if (roleTaxawu == null) {
                writeUnauthorized(response, "Accès non autorisé à l'application Taxawu");
                return;
            }

            // Construction du principal Taxawu enrichi
            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    validation.userId(),
                    validation.email(),
                    validation.nomComplet(),
                    roleTaxawu,
                    validation.roles() != null ? validation.roles() : List.of()
            );

            List<SimpleGrantedAuthority> authorities = authenticatedUser.allRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .toList();

            var auth = new UsernamePasswordAuthenticationToken(
                    authenticatedUser, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (TokenRejectedException ex) {
            writeUnauthorized(response, ex.getMessage());
        } catch (PortailUnavailableException ex) {
            log.error("Portail IDP indisponible : {}", ex.getMessage());
            writeServiceUnavailable(response, ex.getMessage());
        } catch (Exception ex) {
            log.error("Erreur inattendue dans JwtAuthFilter", ex);
            writeUnauthorized(response, "Erreur d'authentification");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"type":"https://daef.sn/errors/unauthorized",
                 "status":401,
                 "title":"Non autorisé",
                 "detail":"%s"}
                """.formatted(message));
    }

    private void writeServiceUnavailable(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"type":"https://daef.sn/errors/service-unavailable",
                 "status":503,
                 "title":"Service indisponible",
                 "detail":"%s"}
                """.formatted(message));
    }
}
