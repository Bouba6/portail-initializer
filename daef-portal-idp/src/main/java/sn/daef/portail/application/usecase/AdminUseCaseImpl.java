package sn.daef.portail.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.daef.portail.domain.exception.UserNotFoundException;
import sn.daef.portail.domain.model.AppAccess;
import sn.daef.portail.domain.port.input.AdminUseCase;
import sn.daef.portail.domain.port.output.AppAccessRepository;
import sn.daef.portail.domain.port.output.UserAccountRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUseCaseImpl implements AdminUseCase {

    private final AppAccessRepository appAccessRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public void assignAccess(UUID userId, String appCode, String role) {
        // Vérification que le compte existe avant d'assigner
        userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        appAccessRepository.assignAccess(userId, appCode, role);
    }

    @Override
    @Transactional
    public void revokeAccess(UUID userId, String appCode) {
        appAccessRepository.revokeAccess(userId, appCode);
    }

    @Override
    public List<AppAccess> getAccesses(UUID userId) {
        return appAccessRepository.findByUserId(userId);
    }
}
