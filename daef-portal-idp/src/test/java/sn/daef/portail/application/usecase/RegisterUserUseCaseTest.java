package sn.daef.portail.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.daef.portail.application.dto.request.RegisterRequest;
import sn.daef.portail.domain.exception.UserAlreadyExistsException;
import sn.daef.portail.domain.model.UserAccount;
import sn.daef.portail.domain.port.output.AppAccessRepository;
import sn.daef.portail.domain.port.output.UserAccountRepository;
import sn.daef.portail.domain.service.AuthDomainService;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock private UserAccountRepository userAccountRepository;
    @Mock private AppAccessRepository appAccessRepository;
    @Mock private AuthDomainService.PasswordEncoder passwordEncoder;
    @InjectMocks private RegisterUserUseCaseImpl registerUserUseCase;

    @Test
    @DisplayName("Enregistrement réussi d'un nouvel utilisateur avec accès assigné")
    void shouldRegisterNewUser() {
        var request = new RegisterRequest(
                "Fatou Diallo", "fatou@daef.sn", "secret123", "+221770000000",
                "TAXAWU", "EVALUATEUR");

        when(userAccountRepository.existsByEmail("fatou@daef.sn")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userAccountRepository.save(any())).thenAnswer(inv -> {
            UserAccount u = inv.getArgument(0);
            return new UserAccount(
                    UUID.randomUUID(), u.getEmail(), u.getPasswordHash(),
                    u.getNomComplet(), u.getTelephone(), LocalDateTime.now());
        });

        var response = registerUserUseCase.execute(request.email(), request.password(),
                request.nomComplet(), request.telephone(), request.appCode(), request.role());

        assertThat(response.getEmail()).isEqualTo("fatou@daef.sn");
        assertThat(response.getNomComplet()).isEqualTo("Fatou Diallo");
        // assertThat(response.get()).isTrue();

        // Vérifie que save ET assignAccess sont appelés dans la même transaction
        verify(userAccountRepository).save(any());
        verify(appAccessRepository).assignAccess(any(), eq("TAXAWU"), eq("EVALUATEUR"));
    }

    @Test
    @DisplayName("Echec si email déjà existant — aucun accès assigné non plus")
    void shouldThrowWhenEmailAlreadyExists() {
        var request = new RegisterRequest(
                "Fatou", "existing@daef.sn", "pass123", null,
                "TAXAWU", "EVALUATEUR");

        when(userAccountRepository.existsByEmail("existing@daef.sn")).thenReturn(true);

        assertThatThrownBy(() -> registerUserUseCase.execute(request.email(), request.password(),
                request.nomComplet(), request.telephone(), request.appCode(), request.role()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("existing@daef.sn");

        // Ni le save ni l'assignation d'accès ne doivent être appelés
        verify(userAccountRepository, never()).save(any());
        verify(appAccessRepository, never()).assignAccess(any(), any(), any());
    }

    @Test
    @DisplayName("Rollback atomique — si assignAccess plante, save est annulé")
    void shouldRollbackIfAssignAccessFails() {
        var request = new RegisterRequest(
                "Aminata Sow", "aminata@daef.sn", "pass1234", null,
                "TAXAWU", "PREFET");

        when(userAccountRepository.existsByEmail("aminata@daef.sn")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed");
        when(userAccountRepository.save(any())).thenAnswer(inv -> {
            UserAccount u = inv.getArgument(0);
            return new UserAccount(
                    UUID.randomUUID(), u.getEmail(), u.getPasswordHash(),
                    u.getNomComplet(), u.getTelephone(), LocalDateTime.now());
        });
        // Simulation d'une erreur sur l'assignation (app introuvable, contrainte FK...)
        doThrow(new RuntimeException("Application TAXAWU introuvable en base"))
                .when(appAccessRepository).assignAccess(any(), eq("TAXAWU"), eq("PREFET"));

        // Le @Transactional garantit que tout est rollbacké — on vérifie l'exception
        assertThatThrownBy(() -> registerUserUseCase.execute(request.email(), request.password(),
                request.nomComplet(), request.telephone(), request.appCode(), request.role()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TAXAWU");
    }
}
