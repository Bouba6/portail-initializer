package sn.daef.portail.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.daef.portail.infrastructure.persistence.entity.UserAccountEntity;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {
    Optional<UserAccountEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
