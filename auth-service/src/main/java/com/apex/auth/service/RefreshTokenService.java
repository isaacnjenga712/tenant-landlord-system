package com.apex.auth.service;

import com.apex.auth.entity.RefreshToken;
import com.apex.auth.entity.User;
import com.apex.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpirationMs;

    /**
     * Creates a new refresh token for the user.
     *
     * <p>Design note: this does NOT revoke existing tokens. Each login creates
     * an independent session — multiple devices can hold valid refresh tokens
     * simultaneously. This is the standard behavior (see: OAuth 2.0, most SaaS
     * auth systems). It also eliminates row-lock contention on the
     * refresh_tokens table during concurrent logins.
     *
     * <p>If you need "single session per user" semantics, do it explicitly on
     * logout or via an admin action, not implicitly on login.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        String tokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        log.debug("Created refresh token for user id={} (new session)", user.getId());
        return saved;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Rotates a refresh token: marks the used token revoked, issues a fresh one.
     * Called only by /api/v1/auth/refresh (not by login).
     */
    @Transactional
    public RefreshToken verifyAndRotate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        // Revoke the used token (single-row UPDATE — no contention from other logins)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.debug("Rotated refresh token for user id={}", refreshToken.getUser().getId());

        // Issue a fresh token — does NOT revoke other sessions
        return createRefreshToken(refreshToken.getUser());
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.info("Deleted all refresh tokens for user id={}", user.getId());
    }
}