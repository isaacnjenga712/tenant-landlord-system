package com.rentflow.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.access.secret}")
    private String secret;

    // Paths that don't require authentication.
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh",
        "/api/v1/auth/callback",
        "/actuator",
        "/ws/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip public paths
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String token = authHeader.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            Object role = claims.get("role");

            // Enforce ADMIN role for /api/v1/admin/**
            if (path.startsWith("/api/v1/admin/")) {
                if (role == null || !"ADMIN".equals(role.toString())) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            ServerHttpRequest.Builder mutated = exchange.getRequest().mutate();

            String publicId = claims.get("publicId", String.class);
            if (publicId != null && !publicId.isBlank()) {
                mutated.header("X-User-Id", publicId);
                mutated.header("X-User-PublicId", publicId);
            } else {
                mutated.header("X-User-Email", claims.getSubject());
            }

            if (role != null) {
                mutated.header("X-User-Role", role.toString());
            }

            return chain.filter(exchange.mutate().request(mutated.build()).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}