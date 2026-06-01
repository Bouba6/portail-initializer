package sn.daef.portail.domain.port.input;

import sn.daef.portail.domain.model.AppAccess;

import java.util.List;
import java.util.UUID;

/**
 * Input Port : récupère les accès applicatifs d'un utilisateur.
 *
 * RÈGLE HEXAGONALE : retourne List<AppAccess> (modèle domaine),
 * PAS List<AppAccessResponse> (DTO application).
 * C'est le controller qui mappe AppAccess → AppAccessResponse.
 */
public interface GetUserAppsUseCase {
    List<AppAccess> execute(UUID userId);
}
