package com.apex.auth.service;

import com.apex.auth.entity.RefreshToken;
import com.apex.auth.entity.User;
import com.apex.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Invalidate any existing valid refresh token for this user
        refreshTokenRepository.findByUserAndRevokedFalse(user)
                .ifPresent(existing -> {
                    existing.setRevoked(true);
                    refreshTokenRepository.save(existing);
                });

        // Generate a new random token string (optional, we could use JWT, but we use JWT for refresh as well)
        // We'll generate a JWT refresh token which we store as the token string.
        String tokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

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

        // Token is valid; rotate: revoke old, create new
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return createRefreshToken(refreshToken.getUser());
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
