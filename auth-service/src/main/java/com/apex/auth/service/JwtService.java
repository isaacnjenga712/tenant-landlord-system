package com.apex.auth.service;

import com.apex.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.access.expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    // ── FIX: derive keys ONCE at startup, not per-request ──────────────────
    private SecretKey accessKey;
    private SecretKey refreshKey;

    @PostConstruct
    public void init() {
        if (accessSecret == null || accessSecret.isBlank()) {
            throw new IllegalStateException(
                    "jwt.access.secret is not configured — auth-service cannot sign tokens");
        }
        if (refreshSecret == null || refreshSecret.isBlank()) {
            throw new IllegalStateException(
                    "jwt.refresh.secret is not configured — auth-service cannot sign tokens");
        }

        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));

        log.info("JwtService initialized: accessKeyAlgorithm={} accessSecretLength={}, " +
                        "refreshKeyAlgorithm={} refreshSecretLength={}",
                accessKey.getAlgorithm(), accessSecret.length(),
                refreshKey.getAlgorithm(), refreshSecret.length());
    }

    // ============================================================
    // ACCESS TOKEN
    // ============================================================

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, accessExpiration, accessKey);
    }

    public String extractUsernameFromAccessToken(String token) {
        return extractClaim(token, Claims::getSubject, accessKey);
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        final String username = extractUsernameFromAccessToken(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, accessKey);
    }

    // ============================================================
    // REFRESH TOKEN
    // ============================================================

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration, refreshKey);
    }

    public String extractUsernameFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, refreshKey);
    }

    public boolean validateRefreshToken(String token) {
        return !isTokenExpired(token, refreshKey);
    }

    public Date extractExpirationFromRefreshToken(String token) {
        return extractClaim(token, Claims::getExpiration, refreshKey);
    }

    // ============================================================
    // INTERNAL — now takes a pre-built SecretKey, not a raw string
    // ============================================================

    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              Long expiration,
                              SecretKey signingKey) {
        String publicId = null;
        String role = null;
        String orgId = null;
        if (userDetails instanceof User user) {
            publicId = user.getPublicId() != null ? user.getPublicId().toString() : null;
            role = user.getRole() != null ? user.getRole().name() : null;
            orgId = user.getOrgId() != null ? user.getOrgId().toString() : null;
        }

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .claim("publicId", publicId)
                .claim("role", role)
                .claim("org_id", orgId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey)
                .compact();
    }

    private <T> T extractClaim(String token,
                               Function<Claims, T> claimsResolver,
                               SecretKey signingKey) {
        final Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token, SecretKey signingKey) {
        return extractClaim(token, Claims::getExpiration, signingKey).before(new Date());
    }
}