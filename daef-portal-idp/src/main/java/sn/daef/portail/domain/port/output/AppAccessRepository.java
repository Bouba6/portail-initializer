package sn.daef.portail.domain.port.output;

import sn.daef.portail.domain.model.AppAccess;
import java.util.List;
import java.util.UUID;

/**
 * Output Port : gestion de la table user_app_permissions.
 * Détermine quelles applications sont visibles dans le Launchpad pour un user.
 */
public interface AppAccessRepository {
    List<AppAccess> findByUserId(UUID userId);
    void assignAccess(UUID userId, String appCode, String role);
    void revokeAccess(UUID userId, String appCode);
}
