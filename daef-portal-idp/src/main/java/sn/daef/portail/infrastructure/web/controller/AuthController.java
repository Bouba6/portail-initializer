package sn.daef.portail.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.daef.portail.application.dto.request.LoginRequest;
import sn.daef.portail.application.dto.request.RegisterRequest;
import sn.daef.portail.application.dto.response.AppAccessResponse;
import sn.daef.portail.application.dto.response.LoginResponse;
import sn.daef.portail.application.dto.response.TokenValidationResponse;
import sn.daef.portail.application.dto.response.UserResponse;
import sn.daef.portail.domain.model.TokenValidation;
import sn.daef.portail.domain.model.UserAccount;
import sn.daef.portail.domain.port.input.LoginUseCase;
import sn.daef.portail.domain.port.input.LogoutUseCase;
import sn.daef.portail.domain.port.input.RegisterUserUseCase;
import sn.daef.portail.domain.port.input.ValidateTokenUseCase;
import sn.daef.portail.infrastructure.security.jwt.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints SSO de l'IDP DAEF")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final JwtService jwtService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau compte utilisateur DAEF")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        // Le controller déstructure le DTO et passe les primitives au port domaine
        UserAccount saved = registerUserUseCase.execute(
                request.email(),
                request.password(),
                request.nomComplet(),
                request.telephone(),
                request.appCode(),
                request.role()
        );
        // Le controller construit le DTO de réponse depuis le modèle domaine
        return toUserResponse(saved);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion — retourne JWT + liste des applications accessibles")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // Le controller extrait les primitives du DTO
        UserAccount account = loginUseCase.execute(request.email(), request.password());

        // Génération du token JWT (infrastructure — pas dans le domaine)
        String token = jwtService.generateToken(account);

        // Mapping domaine → DTO de réponse
        List<AppAccessResponse> appResponses = account.getAppAccesses().stream()
                .map(a -> new AppAccessResponse(
                        a.appCode(), a.appName(), a.appBaseUrl(), a.appIconPath(), a.role()))
                .toList();

        return new LoginResponse(token, jwtService.getExpirationSeconds(),
                account.getNomComplet(), account.getEmail(), appResponses);
    }

    @GetMapping("/validate")
    @Operation(summary = "Valider un token JWT (usage interne — Feign)")
    public TokenValidationResponse validate(
            @RequestHeader("Authorization") String bearerToken) {
        TokenValidation validation = validateTokenUseCase.execute(bearerToken);
        return toTokenValidationResponse(validation);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Révoquer un token (blacklist Redis)")
    public void logout(@RequestHeader("Authorization") String bearerToken) {
        logoutUseCase.execute(bearerToken);
    }

    // ── Mappers privés : domaine → DTO (appartiennent à la couche infrastructure/web) ──

    private UserResponse toUserResponse(UserAccount account) {
        return new UserResponse(
                account.getId(),
                account.getNomComplet(),
                account.getEmail(),
                account.getTelephone(),
                account.isActif(),
                account.getCreatedAt()
        );
    }

    private TokenValidationResponse toTokenValidationResponse(TokenValidation v) {
        List<AppAccessResponse> apps = v.appAccesses().stream()
                .map(a -> new AppAccessResponse(
                        a.appCode(), a.appName(), a.appBaseUrl(), a.appIconPath(), a.role()))
                .toList();
        return new TokenValidationResponse(
                v.valid(), v.userId(), v.email(), v.nomComplet(), v.roles(), apps);
    }
}
