package sn.daef.portail.domain.port.input;

import sn.daef.portail.domain.model.UserAccount;

/**
 * Input Port : création d'un compte utilisateur DAEF.
 *
 * RÈGLE HEXAGONALE : pas de DTO ici. Le port travaille avec des primitives.
 * La couche application (RegisterUserUseCaseImpl) fait le pont entre
 * le RegisterRequest (DTO) et ce port.
 */
public interface RegisterUserUseCase {
    /**
     * @param email      email unique de l'utilisateur
     * @param password   mot de passe en clair (sera encodé dans l'impl)
     * @param nomComplet nom affiché
     * @param telephone  optionnel
     * @param appCode    code de l'application d'accès initial (ex: "TAXAWU")
     * @param role       rôle initial sur cette application (ex: "ADMIN")
     * @return           le UserAccount créé et persisté
     */
    UserAccount execute(String email, String password,
                        String nomComplet, String telephone,
                        String appCode, String role);
}
