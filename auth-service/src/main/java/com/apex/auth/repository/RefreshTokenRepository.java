package com.apex.auth.repository;

import com.apex.auth.entity.RefreshToken;
import com.apex.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserAndRevokedFalse(User user);
    void deleteByUser(User user);
}
