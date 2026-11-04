package com.property.notification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Maps domain exceptions to HTTP status codes.
 *
 * <p>In particular, ResourceNotFoundException → 404. The cross-tenant write
 * probe in the load test sends org-B's X-Org-Id with org-A's notification ID.
 * The service correctly rejects the request with ResourceNotFoundException
 * (the notification doesn't belong to org B), but without this handler Spring
 * returns 500. The load-test classifier treats 500 as INCONCLUSIVE and 404
 * as PASS, so the missing handler made every write probe look inconclusive.
 *
 * <p>This is also correct HTTP semantics: a resource that doesn't exist
 * (in the caller's scope) is a 404, not a 500.
 */
@RestControllerAdvice
public class NotificationExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException e) {
        log.debug("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Not Found",
                        "message", e.getMessage() != null ? e.getMessage() : "Resource not found"
                ));
    }
}