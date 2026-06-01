package sn.daef.portail.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.daef.portail.domain.exception.AccountDisabledException;
import sn.daef.portail.domain.exception.InvalidCredentialsException;
import sn.daef.portail.domain.model.UserAccount;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Test unitaire pur — 0 annotation Spring, 0 mock framework.
 * Le domaine est testable en isolation totale.
 */
class AuthDomainServiceTest {

    private AuthDomainService authDomainService;
    private UserAccount activeAccount;
    private UserAccount disabledAccount;

    @BeforeEach
    void setUp() {
        // PasswordEncoder en inline pour le test — pas de Spring Security ici
        AuthDomainService.PasswordEncoder encoder = new AuthDomainService.PasswordEncoder() {
            @Override
            public String encode(String rawPassword) { return "hashed_" + rawPassword; }
            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return encodedPassword.equals("hashed_" + rawPassword);
            }
        };
        authDomainService = new AuthDomainService(encoder);

        activeAccount = new UserAccount(
                UUID.randomUUID(), "fatou@daef.sn",
                "hashed_secret123", "Fatou Diallo", "+221770000000",
                LocalDateTime.now()
        );

        disabledAccount = new UserAccount(
                UUID.randomUUID(), "marie@daef.sn",
                "hashed_pass", "Marie Ndiaye", null,
                LocalDateTime.now()
        );
        disabledAccount.desactiver();
    }

    @Test
    @DisplayName("Authentification réussie avec bon password")
    void shouldAuthenticateSuccessfully() {
        var result = authDomainService.authentifier(activeAccount, "secret123");
        assertThat(result).isEqualTo(activeAccount);
    }

    @Test
    @DisplayName("Echec si mot de passe incorrect")
    void shouldThrowOnWrongPassword() {
        assertThatThrownBy(() -> authDomainService.authentifier(activeAccount, "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Echec si compte désactivé")
    void shouldThrowOnDisabledAccount() {
        assertThatThrownBy(() -> authDomainService.authentifier(disabledAccount, "pass"))
                .isInstanceOf(AccountDisabledException.class)
                .hasMessageContaining("marie@daef.sn");
    }
}
