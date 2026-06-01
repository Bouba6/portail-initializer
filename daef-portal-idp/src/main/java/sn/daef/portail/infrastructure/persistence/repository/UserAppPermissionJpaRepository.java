package sn.daef.portail.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import sn.daef.portail.infrastructure.persistence.entity.UserAppPermissionEntity;

public interface UserAppPermissionJpaRepository extends JpaRepository<UserAppPermissionEntity, UUID> {

    /**
     * JOIN FETCH évite le N+1 lors du chargement des permissions avec leur application.
     * Hot path : appelé à chaque login et à chaque validation de token.
     */
    @Query("SELECT p FROM UserAppPermissionEntity p " +
           "JOIN FETCH p.application " +
           "WHERE p.user.id = :userId")
    List<UserAppPermissionEntity> findByUserIdWithApp(@Param("userId") UUID userId);

    /**
     * CORRIGÉ : Spring Data ne peut pas dériver une query traversant une relation
     * (p.application.code). Il faut un @Query explicite.
     * @Modifying + @Transactional sont obligatoires pour les DELETE avec @Query.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserAppPermissionEntity p " +
           "WHERE p.user.id = :userId " +
           "AND p.application.code = :appCode")
    void deleteByUserIdAndApplicationCode(@Param("userId") UUID userId,
                                          @Param("appCode") String appCode);
}
