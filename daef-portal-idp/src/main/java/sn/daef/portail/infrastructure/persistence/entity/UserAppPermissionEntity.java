package sn.daef.portail.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Table de jonction : user_id × app_id × role
 * C'est ici que vit la logique "qui voit quoi sur le Launchpad".
 */
@Entity
@Table(name = "user_app_permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "app_id"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserAppPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccountEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private ApplicationEntity application;

    @Column(nullable = false, length = 50)
    private String role;  // ex: EVALUATEUR, PREFET, ADMIN, PROMOTRICE
}
