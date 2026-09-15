package com.rentflow.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/service")
    public Mono<ResponseEntity<Map<String, Object>>> serviceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status",    503,
                        "error",     "Service Unavailable",
                        "message",   "The requested service is temporarily unavailable. Please retry.",
                        "timestamp", Instant.now().toString()
                )));
    }

    @PostMapping("/service")
    public Mono<ResponseEntity<Map<String, Object>>> serviceFallbackPost() {
        return serviceFallback();
    }

    @GetMapping("/mpesa")
    public Mono<ResponseEntity<Map<String, Object>>> mpesaFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status",    503,
                        "error",     "M-Pesa Service Unavailable",
                        "message",   "The M-Pesa integration is temporarily unavailable.",
                        "timestamp", Instant.now().toString()
                )));
    }

    @PostMapping("/mpesa")
    public Mono<ResponseEntity<Map<String, Object>>> mpesaFallbackPost() {
        return mpesaFallback();
    }
}
