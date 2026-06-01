package sn.daef.portail.domain.port.output;

import java.util.Optional;
import java.util.UUID;

import sn.daef.portail.domain.model.UserAccount;

/**
 * Output Port : contrat de persistance — le domaine définit CE DONT il a besoin,
 * l'infrastructure l'implémente (JPA, in-memory pour tests...).
 */
public interface UserAccountRepository {
    UserAccount save(UserAccount account);
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findById(UUID id);
    boolean existsByEmail(String email);
}


// cat > /Users/admin/Downloads/daef-portal-idp/src/main/resources/db/migration/V2__seed_applications.sql

