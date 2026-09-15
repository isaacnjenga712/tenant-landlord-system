package com.apex.auth.exception;

/**
 * Thrown when a refresh token cannot be validated.
 *
 * HTTP status: 401 Unauthorized
 * Reason: The client's credentials (refresh token) are invalid or expired.
 *
 * The reason field is for server-side logging only and is NOT returned to
 * the client — we never want to tell an attacker *why* a token was rejected.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    private final Reason reason;

    public enum Reason {
        NOT_FOUND,
        REVOKED,
        EXPIRED,
        MALFORMED
    }

    public InvalidRefreshTokenException(Reason reason) {
        super(buildMessage(reason));
        this.reason = reason;
    }

    public InvalidRefreshTokenException(Reason reason, Throwable cause) {
        super(buildMessage(reason), cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    private static String buildMessage(Reason reason) {
        return switch (reason) {
            case NOT_FOUND -> "Refresh token not found";
            case REVOKED   -> "Refresh token has been revoked";
            case EXPIRED   -> "Refresh token has expired";
            case MALFORMED -> "Refresh token is malformed";
        };
    }
}
