package com.apex.auth.service;

import com.apex.auth.dto.AuthenticationResponse;

import com.apex.auth.dto.LoginRequest;
import com.apex.auth.dto.RegisterRequest;
import com.apex.auth.entity.RefreshToken;
import com.apex.auth.entity.Role;
import com.apex.auth.entity.User;
import com.apex.auth.exception.EmailAlreadyRegisteredException;
import com.apex.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    // ---------- MOCKS (all 6 required by AuthenticationService's constructor) ----------
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("t@x.com")
                .password("hashed")
                .fullName("Test")
                .role(Role.TENANT)
                .enabled(true)
                .build();
    }

    // ============================================================
    // REGISTER
    // ============================================================

    @Test
    @DisplayName("register() saves user, publishes event, returns tokens")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("t@x.com");
        req.setPassword("pw");
        req.setFullName("Test");
        req.setRole(Role.TENANT);

        when(userRepository.existsByEmail("t@x.com")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(
                RefreshToken.builder()
                        .token("refresh")
                        .user(user)
                        .expiryDate(Instant.now().plusSeconds(3600))
                        .revoked(false)
                        .build()
        );

        AuthenticationResponse res = authenticationService.register(req);

        assertThat(res.getAccessToken()).isEqualTo("access");
        assertThat(res.getRefreshToken()).isEqualTo("refresh");
        assertThat(res.getRole()).isEqualTo(Role.TENANT);
        assertThat(res.getEmail()).isEqualTo("t@x.com");

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("pw");
        verify(kafkaEventPublisher).publishUserRegistered(user);
    }

    @Test
    @DisplayName("register() throws EmailAlreadyRegisteredException on duplicate email")
    void register_duplicate() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("t@x.com");
        req.setPassword("pw");
        req.setFullName("Test");
        req.setRole(Role.TENANT);

        when(userRepository.existsByEmail("t@x.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(req))
                .isInstanceOf(EmailAlreadyRegisteredException.class)
                .hasMessageContaining("t@x.com");

        verify(userRepository, never()).save(any());
        verify(kafkaEventPublisher, never()).publishUserRegistered(any());
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @Test
    @DisplayName("login() publishes UserLoggedInEvent after successful authentication")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("t@x.com");
        req.setPassword("pw");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(
                RefreshToken.builder()
                        .token("refresh")
                        .user(user)
                        .expiryDate(Instant.now().plusSeconds(3600))
                        .revoked(false)
                        .build()
        );

        AuthenticationResponse res = authenticationService.login(req);

        assertThat(res.getAccessToken()).isEqualTo("access");
        assertThat(res.getRefreshToken()).isEqualTo("refresh");
        assertThat(res.getRole()).isEqualTo(Role.TENANT);

        verify(kafkaEventPublisher).publishUserLoggedIn(user);
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    @DisplayName("login() does NOT publish an event when credentials are wrong")
    void login_badCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("t@x.com");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authenticationService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        verify(kafkaEventPublisher, never()).publishUserLoggedIn(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    // ============================================================
    // REFRESH TOKEN
    // ============================================================

    @Test
    @DisplayName("refreshAccessToken() issues new access + refresh tokens")
    void refresh_success() {
        RefreshToken rotated = RefreshToken.builder()
                .token("new-refresh")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenService.verifyAndRotate("old")).thenReturn(rotated);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");

        AuthenticationResponse res = authenticationService.refreshAccessToken("old");

        assertThat(res.getAccessToken()).isEqualTo("new-access");
        assertThat(res.getRefreshToken()).isEqualTo("new-refresh");
        assertThat(res.getRole()).isEqualTo(Role.TENANT);

        verify(refreshTokenService).verifyAndRotate("old");
    }

    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    @Test
    @DisplayName("changePassword() updates password, revokes tokens, publishes event")
    void changePassword_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("hashed-new");

        authenticationService.changePassword(1L, "old", "new");

        verify(userRepository).save(user);
        verify(refreshTokenService).deleteByUser(user);
        verify(kafkaEventPublisher).publishUserPasswordChanged(user);
    }

    @Test
    @DisplayName("changePassword() throws when old password is wrong — no event published")
    void changePassword_wrongOld() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.changePassword(1L, "wrong", "new"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("incorrect");

        verify(userRepository, never()).save(any());
        verify(refreshTokenService, never()).deleteByUser(any());
        verify(kafkaEventPublisher, never()).publishUserPasswordChanged(any());
    }

    // ============================================================
    // CHANGE ROLE
    // ============================================================

    @Test
    @DisplayName("changeRole() publishes old/new role")
    void changeRole_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authenticationService.changeRole(1L, Role.LANDLORD);

        assertThat(user.getRole()).isEqualTo(Role.LANDLORD);
        verify(userRepository).save(user);
        verify(kafkaEventPublisher).publishUserRoleChanged(user, Role.TENANT, Role.LANDLORD);
    }

    @Test
    @DisplayName("changeRole() is a no-op when role is unchanged")
    void changeRole_noOp() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authenticationService.changeRole(1L, Role.TENANT);

        verify(userRepository, never()).save(any());
        verify(kafkaEventPublisher, never()).publishUserRoleChanged(any(), any(), any());
    }

    // ============================================================
    // DISABLE USER
    // ============================================================

    @Test
    @DisplayName("disableUser() flips enabled, revokes tokens, publishes event")
    void disableUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authenticationService.disableUser(1L, "policy violation");

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
        verify(refreshTokenService).deleteByUser(user);
        verify(kafkaEventPublisher).publishUserDisabled(user, "policy violation");
    }

    @Test
    @DisplayName("disableUser() is a no-op when already disabled")
    void disableUser_alreadyDisabled() {
        user.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authenticationService.disableUser(1L, "x");

        verify(userRepository, never()).save(any());
        verify(kafkaEventPublisher, never()).publishUserDisabled(any(), any());
    }
}
