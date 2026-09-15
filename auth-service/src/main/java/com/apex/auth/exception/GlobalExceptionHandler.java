package com.apex.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------------
    // 409 CONFLICT — email already registered
    // ---------------------------------------------------------------

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyRegistered(
            EmailAlreadyRegisteredException ex,
            HttpServletRequest request) {

        log.warn("Registration conflict: {}", ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ---------------------------------------------------------------
    // 401 UNAUTHORIZED — invalid / revoked / expired refresh token
    // ---------------------------------------------------------------

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex,
            HttpServletRequest request) {

        log.warn("Invalid refresh token ({}): {}", ex.getReason(), ex.getMessage());

        // Generic message — never leak "expired" vs "revoked"
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid refresh token")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex,
            HttpServletRequest request) {

        log.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Bad credentials")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // ---------------------------------------------------------------
    // 503 SERVICE UNAVAILABLE — Kafka publish failed
    // ---------------------------------------------------------------

    @ExceptionHandler(EventPublishException.class)
    public ResponseEntity<ErrorResponse> handleEventPublish(
            EventPublishException ex,
            HttpServletRequest request) {

        log.error("Event publish failure: eventType={} topic={}",
                ex.getEventType(), ex.getTopic(), ex);

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message("Unable to process request — please retry shortly")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    // ---------------------------------------------------------------
    // 400 BAD REQUEST — validation
    // ---------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            fieldErrors.put(field, err.getDefaultMessage());
        });

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .details(fieldErrors)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ---------------------------------------------------------------
    // 400 BAD REQUEST — generic business errors
    // ---------------------------------------------------------------

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Business error: {}", ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ---------------------------------------------------------------
    // 500 INTERNAL SERVER ERROR — catch-all
    // ---------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception at {}", request.getRequestURI(), ex);

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
