package com.apex.auth.service;

import com.apex.auth.dto.AuthenticationResponse;
import com.apex.auth.dto.LoginRequest;
import com.apex.auth.dto.RegisterRequest;
import com.apex.auth.entity.RefreshToken;
import com.apex.auth.entity.Role;
import com.apex.auth.entity.User;
import com.apex.auth.exception.EmailAlreadyRegisteredException;
import com.apex.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final KafkaEventPublisher kafkaEventPublisher;

    // ============================================================
    // REGISTER
    // ============================================================

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("Registered new user: id={} publicId={} email={} role={}",
                user.getId(), user.getPublicId(), user.getEmail(), user.getRole());

        kafkaEventPublisher.publishUserRegistered(user);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildResponse(user, accessToken, refreshToken.getToken());
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        log.info("User logged in: id={} publicId={} email={}",
                user.getId(), user.getPublicId(), user.getEmail());

        kafkaEventPublisher.publishUserLoggedIn(user);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildResponse(user, accessToken, refreshToken.getToken());
    }

    // ============================================================
    // REFRESH TOKEN
    // ============================================================

    @Transactional
    public AuthenticationResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken newRefreshToken = refreshTokenService.verifyAndRotate(refreshTokenValue);
        User user = newRefreshToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);

        log.debug("Access token refreshed for user id={} email={}",
                user.getId(), user.getEmail());

        return buildResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user id={} email={}", user.getId(), user.getEmail());

        refreshTokenService.deleteByUser(user);

        kafkaEventPublisher.publishUserPasswordChanged(user);
    }

    // ============================================================
    // CHANGE ROLE (admin)
    // ============================================================

    @Transactional
    public void changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role oldRole = user.getRole();
        if (oldRole == newRole) {
            return;
        }

        user.setRole(newRole);
        userRepository.save(user);

        log.info("Role changed for user id={}: {} → {}", user.getId(), oldRole, newRole);

        kafkaEventPublisher.publishUserRoleChanged(user, oldRole, newRole);
    }

    // ============================================================
    // DISABLE USER (admin)
    // ============================================================

    @Transactional
    public void disableUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            return;
        }

        user.setEnabled(false);
        userRepository.save(user);

        refreshTokenService.deleteByUser(user);

        log.info("User disabled: id={} email={} reason={}",
                user.getId(), user.getEmail(), reason);

        kafkaEventPublisher.publishUserDisabled(user, reason);
    }

    // ============================================================
    // HELPER
    // ============================================================

    private AuthenticationResponse buildResponse(User user, String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .id(user.getPublicId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .email(user.getEmail())
                .build();
    }
}