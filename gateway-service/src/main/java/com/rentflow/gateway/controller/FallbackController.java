
package com.rentflow.gateway.controller;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.Map;



@RestController

@RequestMapping("/fallback")

public class FallbackController {

    @GetMapping("/service")

    public ResponseEntity<Map<String, Object>> serviceFallback() {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)

            .body(Map.of("error", "Service temporarily unavailable", "code", 503));

    }

    @GetMapping("/mpesa")

    public ResponseEntity<Map<String, Object>> mpesaFallback() {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)

            .body(Map.of("error", "M-Pesa service busy, retry", "code", 503));

    }

}

