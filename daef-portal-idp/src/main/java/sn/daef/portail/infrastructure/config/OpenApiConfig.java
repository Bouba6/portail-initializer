package sn.daef.portail.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI daefPortalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DAEF Portal IDP — API")
                        .description("""
                                Identity Provider centralisé de l'écosystème DAEF.
                                Gère l'authentification SSO pour Taxawu (FNCF) et Suquali (FNEF).
                                
                                **Endpoints internes** (consommés via OpenFeign par les autres services) :
                                - `GET /api/auth/validate` — validation de token
                                - `GET /api/users/{id}/apps` — liste des apps du Launchpad
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DAEF Tech Team")
                                .email("tech@daef.sn")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort)
                                    .description("Environnement local"),
                        new Server().url("https://portail.daef.sn")
                                    .description("Production")
                ))
                // Schéma JWT Bearer pour Swagger UI
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Token", new SecurityScheme()
                                .name("Bearer Token")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
