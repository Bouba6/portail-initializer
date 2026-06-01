package sn.daef.portail.domain.port.input;

import sn.daef.portail.domain.model.AppAccess;

import java.util.List;
import java.util.UUID;

/**
 * Input Port : opérations d'administration des accès utilisateurs.
 *
 * RÈGLE HEXAGONALE : ce port appartient au domaine — zéro dépendance
 * vers la couche application ou infrastructure.
 * Le AdminController passe par ce port, jamais directement
 * par AppAccessRepository (port output).
 */
public interface AdminUseCase {

    /**
     * Assigne un accès applicatif à un utilisateur existant.
     * Lève UserNotFoundException si l'userId est inconnu.
     * Lève IllegalArgumentException si l'appCode est inconnu.
     */
    void assignAccess(UUID userId, String appCode, String role);

    /**
     * Révoque l'accès d'un utilisateur à une application.
     * Idempotent : ne lève pas d'exception si l'accès n'existe pas.
     */
    void revokeAccess(UUID userId, String appCode);

    /**
     * Liste tous les accès applicatifs d'un utilisateur.
     */
    List<AppAccess> getAccesses(UUID userId);
}
