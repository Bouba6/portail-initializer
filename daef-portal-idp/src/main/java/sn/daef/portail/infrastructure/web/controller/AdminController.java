package sn.daef.portail.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.daef.portail.application.dto.request.AdminUserAccessRequest;
import sn.daef.portail.application.dto.response.AppAccessResponse;
import sn.daef.portail.domain.port.input.AdminUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Panel d'administration des accès utilisateurs.
 *
 * RÈGLE HEXAGONALE RESPECTÉE :
 * - Injecte AdminUseCase (port INPUT) — jamais un port output directement
 * - Fait le mapping AppAccess → AppAccessResponse ici dans la couche web
 * - Le domaine ne sait pas qu'un controller existe
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Gestion des accès utilisateurs — réservé ADMIN")
public class AdminController {

    private final AdminUseCase adminUseCase;

    @PostMapping("/users/{userId}/access")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assigner un accès applicatif à un utilisateur")
    public void assignAccess(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserAccessRequest request) {
        adminUseCase.assignAccess(userId, request.appCode(), request.role());
    }

    @DeleteMapping("/users/{userId}/access/{appCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Révoquer l'accès d'un utilisateur à une application")
    public void revokeAccess(
            @PathVariable UUID userId,
            @PathVariable String appCode) {
        adminUseCase.revokeAccess(userId, appCode);
    }

    @GetMapping("/users/{userId}/access")
    @Operation(summary = "Lister les accès applicatifs d'un utilisateur")
    public List<AppAccessResponse> getUserAccess(@PathVariable UUID userId) {
        // Mapping domaine → DTO fait ici dans la couche web, pas dans le use case
        return adminUseCase.getAccesses(userId).stream()
                .map(a -> new AppAccessResponse(
                        a.appCode(), a.appName(), a.appBaseUrl(), a.appIconPath(), a.role()))
                .toList();
    }
}
