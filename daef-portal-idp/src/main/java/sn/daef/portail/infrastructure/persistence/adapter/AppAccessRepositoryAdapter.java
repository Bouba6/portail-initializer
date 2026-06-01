package sn.daef.portail.infrastructure.persistence.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sn.daef.portail.domain.model.AppAccess;
import sn.daef.portail.domain.port.output.AppAccessRepository;
import sn.daef.portail.infrastructure.persistence.entity.ApplicationEntity;
import sn.daef.portail.infrastructure.persistence.entity.UserAccountEntity;
import sn.daef.portail.infrastructure.persistence.entity.UserAppPermissionEntity;
import sn.daef.portail.infrastructure.persistence.mapper.UserAccountMapper;
import sn.daef.portail.infrastructure.persistence.repository.UserAppPermissionJpaRepository;
import sn.daef.portail.infrastructure.persistence.repository.ApplicationJpaRepository;

@Component
@RequiredArgsConstructor
public class AppAccessRepositoryAdapter implements AppAccessRepository {

    private final UserAppPermissionJpaRepository jpaRepository;
    private final UserAccountMapper mapper;
    private final ApplicationJpaRepository applicationJpaRepository;

    @Override
    public List<AppAccess> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdWithApp(userId)
                .stream()
                .map(mapper::permissionToAppAccess)
                .toList();
    }

@Override
public void assignAccess(UUID userId, String appCode, String role) {
    ApplicationEntity app = applicationJpaRepository.findByCode(appCode)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Application inconnue : " + appCode));

    UserAccountEntity user = new UserAccountEntity();
    user.setId(userId);  // proxy JPA — pas besoin de charger tout le compte

    var permission = UserAppPermissionEntity.builder()
            .user(user)
            .application(app)
            .role(role)
            .build();

    jpaRepository.save(permission);
}

    @Override
    public void revokeAccess(UUID userId, String appCode) {
        jpaRepository.deleteByUserIdAndApplicationCode(userId, appCode);
    }
}
