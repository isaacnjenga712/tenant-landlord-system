package com.apex.auth.exception;

/**
 * Thrown when a registration is attempted with an email that already exists.
 *
 * HTTP status: 409 Conflict
 * Reason: The request is well-formed, but conflicts with existing state.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

    private final String email;

    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: " + email);
        this.email = email;
    }

    public EmailAlreadyRegisteredException(String email, Throwable cause) {
        super("Email already registered: " + email, cause);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
