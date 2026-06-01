package sn.daef.portail.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.daef.portail.domain.model.UserAccount;
import sn.daef.portail.domain.port.output.UserAccountRepository;
import sn.daef.portail.infrastructure.persistence.mapper.UserAccountMapper;
import sn.daef.portail.infrastructure.persistence.repository.UserAccountJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAccountRepositoryAdapter implements UserAccountRepository {

    private final UserAccountJpaRepository jpaRepository;
    private final UserAccountMapper mapper;

    @Override
    public UserAccount save(UserAccount account) {
        var entity = mapper.toEntity(account);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
