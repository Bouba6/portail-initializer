package sn.daef.portail.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sn.daef.portail.application.dto.response.AppAccessResponse;
import sn.daef.portail.domain.port.input.GetUserAppsUseCase;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Accès utilisateurs DAEF — Launchpad")
public class UserController {

    private final GetUserAppsUseCase getUserAppsUseCase;

    /**
     * Appelé par le frontend Launchpad après login.
     * Retourne la liste des applications visibles pour cet utilisateur.
     */
    @GetMapping("/{id}/apps")
    @Operation(summary = "Applications accessibles pour un utilisateur (Launchpad)")
    public List<AppAccessResponse> getUserApps(@PathVariable UUID id) {
        // Use case retourne List<AppAccess> (domaine)
        // Le controller mappe vers AppAccessResponse (DTO) — jamais le use case
        return getUserAppsUseCase.execute(id).stream()
                .map(a -> new AppAccessResponse(
                        a.appCode(), a.appName(), a.appBaseUrl(), a.appIconPath(), a.role()))
                .toList();
    }
}
