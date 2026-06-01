package sn.daef.portail.domain.port.input;

import sn.daef.portail.domain.model.UserAccount;

/**
 * Input Port : authentification d'un utilisateur.
 *
 * RÈGLE HEXAGONALE : le domaine ne connaît PAS les DTOs de la couche application.
 * Le port travaille avec les primitives Java ou les modèles du domaine uniquement.
 * C'est le UseCase impl qui reçoit le DTO et en extrait ce dont le domaine a besoin.
 */
public interface LoginUseCase {
    /**
     * @param email      email de l'utilisateur
     * @param password   mot de passe en clair
     * @return           le UserAccount authentifié avec ses accès chargés
     */
    UserAccount execute(String email, String password);
}
