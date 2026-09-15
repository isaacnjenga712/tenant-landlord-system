package com.apex.auth.service;

import com.apex.auth.entity.RefreshToken;
import com.apex.auth.entity.Role;
import com.apex.auth.entity.User;
import com.apex.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604_800_000L);

        user = User.builder()
                .id(1L)
                .email("tenant@example.com")
                .password("hashed")
                .fullName("Test Tenant")
                .role(Role.TENANT)
                .enabled(true)
                .build();
    }

    // ---------- createRefreshToken ----------

    @Test
    @DisplayName("createRefreshToken() persists a new token when no active token exists")
    void createRefreshToken_freshUser() {
        when(refreshTokenRepository.findByUserAndRevokedFalse(user)).thenReturn(Optional.empty());
        when(jwtService.generateRefreshToken(user)).thenReturn("signed-jwt-refresh");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RefreshToken created = refreshTokenService.createRefreshToken(user);

        assertThat(created.getToken()).isEqualTo("signed-jwt-refresh");
        assertThat(created.getUser()).isEqualTo(user);
        assertThat(created.isRevoked()).isFalse();
        assertThat(created.getExpiryDate()).isAfter(Instant.now());

        verify(refreshTokenRepository).findByUserAndRevokedFalse(user);
        verify(jwtService).generateRefreshToken(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("createRefreshToken() revokes existing active token before issuing a new one")
    void createRefreshToken_revokesOldToken() {
        RefreshToken existing = RefreshToken.builder()
                .id(99L)
                .token("old-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByUserAndRevokedFalse(user)).thenReturn(Optional.of(existing));
        when(jwtService.generateRefreshToken(user)).thenReturn("new-token");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        refreshTokenService.createRefreshToken(user);

        assertThat(existing.isRevoked()).isTrue();
        // save called at least twice: once to revoke old, once to persist new
        verify(refreshTokenRepository, atLeast(2)).save(any(RefreshToken.class));
    }

    // ---------- verifyAndRotate ----------

    @Test
    @DisplayName("verifyAndRotate() throws when token is not found")
    void verifyAndRotate_tokenMissing() {
        when(refreshTokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyAndRotate("nope"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("verifyAndRotate() throws when token is revoked")
    void verifyAndRotate_tokenRevoked() {
        RefreshToken revoked = RefreshToken.builder()
                .token("revoked")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> refreshTokenService.verifyAndRotate("revoked"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    @DisplayName("verifyAndRotate() throws when token is expired")
    void verifyAndRotate_tokenExpired() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(60))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.verifyAndRotate("expired"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("verifyAndRotate() revokes old token and issues a new one for a valid token")
    void verifyAndRotate_success() {
        RefreshToken valid = RefreshToken.builder()
                .id(1L)
                .token("valid-old")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-old")).thenReturn(Optional.of(valid));
        when(refreshTokenRepository.findByUserAndRevokedFalse(user)).thenReturn(Optional.empty());
        when(jwtService.generateRefreshToken(user)).thenReturn("rotated-new");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.verifyAndRotate("valid-old");

        assertThat(valid.isRevoked()).isTrue();
        assertThat(result.getToken()).isEqualTo("rotated-new");
        assertThat(result.isRevoked()).isFalse();
    }

    // ---------- deleteByUser ----------

    @Test
    @DisplayName("deleteByUser() delegates to repository")
    void deleteByUser_delegates() {
        refreshTokenService.deleteByUser(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }
}
