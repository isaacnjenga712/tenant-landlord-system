package com.apex.auth.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("EmailAlreadyRegisteredException → 409")
    void emailAlreadyRegistered_409() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/register");

        ResponseEntity<ErrorResponse> res = handler.handleEmailAlreadyRegistered(
                new EmailAlreadyRegisteredException("x@y.com"), req);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().getMessage()).contains("x@y.com");
    }

    @Test
    @DisplayName("InvalidRefreshTokenException → 401, generic message")
    void invalidRefreshToken_401_genericMessage() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/refresh");

        ResponseEntity<ErrorResponse> res = handler.handleInvalidRefreshToken(
                new InvalidRefreshTokenException(InvalidRefreshTokenException.Reason.EXPIRED),
                req);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().getMessage()).isEqualTo("Invalid refresh token");
        assertThat(res.getBody().getMessage()).doesNotContain("expired");
    }

    @Test
    @DisplayName("EventPublishException → 503")
    void eventPublish_503() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/register");

        ResponseEntity<ErrorResponse> res = handler.handleEventPublish(
                new EventPublishException("UserRegisteredEvent", "auth.user.events",
                        new RuntimeException("broker down")),
                req);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(res.getBody().getMessage()).doesNotContain("broker down");
    }
}
