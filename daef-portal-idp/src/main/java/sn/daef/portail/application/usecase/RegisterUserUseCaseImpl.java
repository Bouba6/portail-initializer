package sn.daef.portail.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.daef.portail.application.dto.request.RegisterRequest;
import sn.daef.portail.domain.exception.UserAlreadyExistsException;
import sn.daef.portail.domain.model.UserAccount;
import sn.daef.portail.domain.port.input.RegisterUserUseCase;
import sn.daef.portail.domain.port.output.AppAccessRepository;
import sn.daef.portail.domain.port.output.UserAccountRepository;
import sn.daef.portail.domain.service.AuthDomainService;

import java.time.LocalDateTime;

/**
 * Couche application : fait le pont entre le DTO HTTP (RegisterRequest)
 * et le port domaine (RegisterUserUseCase).
 *
 * C'est ICI que vit le DTO RegisterRequest — pas dans le domaine.
 * Le port domaine ne voit que des primitives Java.
 */
@Service
@RequiredArgsConstructor
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserAccountRepository userAccountRepository;
    private final AppAccessRepository appAccessRepository;
    private final AuthDomainService.PasswordEncoder passwordEncoder;

    /**
     * Point d'entrée appelé par le controller via le DTO.
     * Le controller déstructure le DTO et passe les primitives.
     */
    @Override
    @Transactional
    public UserAccount execute(String email, String password,
                               String nomComplet, String telephone,
                               String appCode, String role) {

        if (userAccountRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        var account = new UserAccount(
                null,
                email,
                passwordEncoder.encode(password),
                nomComplet,
                telephone,
                LocalDateTime.now()
        );

        var saved = userAccountRepository.save(account);

        // Accès applicatif initial assigné dès la création
        appAccessRepository.assignAccess(saved.getId(), appCode, role);

        return saved;
    }
}
