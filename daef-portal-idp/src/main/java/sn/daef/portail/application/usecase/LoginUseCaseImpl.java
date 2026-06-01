package sn.daef.portail.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.daef.portail.domain.exception.UserNotFoundException;
import sn.daef.portail.domain.model.AppAccess;
import sn.daef.portail.domain.model.UserAccount;
import sn.daef.portail.domain.port.input.LoginUseCase;
import sn.daef.portail.domain.port.output.AppAccessRepository;
import sn.daef.portail.domain.port.output.UserAccountRepository;
import sn.daef.portail.domain.service.AuthDomainService;

import java.util.List;

/**
 * Orchestration du login :
 * 1. Charge le compte depuis le repo
 * 2. Délègue la vérification métier à AuthDomainService
 * 3. Charge les accès applicatifs
 * 4. Retourne le UserAccount enrichi — c'est le controller qui construit la réponse HTTP
 */
@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final UserAccountRepository userAccountRepository;
    private final AppAccessRepository appAccessRepository;
    private final AuthDomainService authDomainService;

    @Override
    public UserAccount execute(String email, String password) {
        var account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        // Règle métier : compte actif + password valide (domaine pur)
        authDomainService.authentifier(account, password);

        // Enrichissement avec les accès applicatifs
        List<AppAccess> accesses = appAccessRepository.findByUserId(account.getId());
        account.assignerAcces(accesses);

        return account;
    }
}
