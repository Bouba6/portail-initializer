package sn.daef.portail.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.daef.portail.infrastructure.persistence.entity.ApplicationEntity;
import java.util.UUID;
import java.util.Optional;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationEntity, UUID> {
    Optional<ApplicationEntity> findByCode(String code);
}
