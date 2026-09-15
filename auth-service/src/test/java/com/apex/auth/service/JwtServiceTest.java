package com.apex.auth.service;

import com.apex.auth.entity.Role;
import com.apex.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String ACCESS_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String REFRESH_SECRET =
            "607164346A576E5A7234753778217A25432A462D4A614E645267556B587032";
    private static final Long ACCESS_EXP = 900_000L;        // 15 min
    private static final Long REFRESH_EXP = 604_800_000L;   // 7 days

    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "accessSecret", ACCESS_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessExpiration", ACCESS_EXP);
        ReflectionTestUtils.setField(jwtService, "refreshSecret", REFRESH_SECRET);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXP);

        user = User.builder()
                .id(1L)
                .email("jane@example.com")
                .password("hashed")
                .fullName("Jane Doe")
                .role(Role.TENANT)
                .enabled(true)
                .build();
    }

    // ---------- ACCESS TOKEN ----------

    @Test
    @DisplayName("generateAccessToken() produces a token whose subject equals the user's email")
    void generateAccessToken_subjectIsEmail() {
        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsernameFromAccessToken(token)).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("validateAccessToken() returns true for a valid token + matching UserDetails")
    void validateAccessToken_valid() {
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.validateAccessToken(token, user)).isTrue();
    }

    @Test
    @DisplayName("validateAccessToken() returns false when the token belongs to a different user")
    void validateAccessToken_wrongUser() {
        String token = jwtService.generateAccessToken(user);

        User other = User.builder()
                .email("other@example.com")
                .password("x")
                .role(Role.LANDLORD)
                .enabled(true)
                .build();

        assertThat(jwtService.validateAccessToken(token, other)).isFalse();
    }

    @Test
    @DisplayName("validateAccessToken() returns false for a token signed with the refresh secret")
    void validateAccessToken_signedWithWrongSecret() {
        String refreshToken = jwtService.generateRefreshToken(user);

        // Passing a refresh token where an access token is expected should fail
        assertThatThrownBy(() -> jwtService.validateAccessToken(refreshToken, user))
                .isInstanceOf(Exception.class);
    }

    // ---------- REFRESH TOKEN ----------

    @Test
    @DisplayName("generateRefreshToken() produces a token with the correct subject")
    void generateRefreshToken_subjectIsEmail() {
        String token = jwtService.generateRefreshToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsernameFromRefreshToken(token)).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("validateRefreshToken() returns true for a freshly generated refresh token")
    void validateRefreshToken_valid() {
        String token = jwtService.generateRefreshToken(user);
        assertThat(jwtService.validateRefreshToken(token)).isTrue();
    }

    @Test
    @DisplayName("extractExpirationFromRefreshToken() returns a future date ~7 days out")
    void extractExpiration_isFuture() {
        String token = jwtService.generateRefreshToken(user);
        Date exp = jwtService.extractExpirationFromRefreshToken(token);

        long millisUntilExpiry = exp.getTime() - System.currentTimeMillis();
        assertThat(millisUntilExpiry).isBetween(
                REFRESH_EXP - 10_000L,
                REFRESH_EXP + 10_000L
        );
    }

    // ---------- SEPARATION ----------

    @Test
    @DisplayName("Access tokens and refresh tokens use different secrets — cross-validation fails")
    void accessAndRefreshSecretsAreDistinct() {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Trying to read an access token with the refresh secret must throw
        assertThatThrownBy(() -> jwtService.extractUsernameFromRefreshToken(accessToken))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> jwtService.extractUsernameFromAccessToken(refreshToken))
                .isInstanceOf(Exception.class);
    }
}
