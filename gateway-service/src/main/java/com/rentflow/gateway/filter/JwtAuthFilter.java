package com.rentflow.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    // Headers we ALWAYS strip from inbound requests and (re)inject from verified JWT.
    // Never trust client-supplied values for these.
    private static final List<String> TRUSTED_HEADERS = List.of(
            "X-Org-Id",
            "X-User-Id",
            "X-User-PublicId",
            "X-User-Email",
            "X-User-Role"
    );

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/callback",
            "/actuator",
            "/ws/"
    );

    @Value("${jwt.access.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Debug: log the secret being used (first 20 chars) once per request
        log.debug("### JwtAuthFilter.secret prefix=[{}] length={}",
                secret == null ? "NULL" : secret.substring(0, Math.min(20, secret.length())),
                secret == null ? 0 : secret.length());

        // 1. ALWAYS sanitize first — no matter the path, no matter what happens next.
        ServerHttpRequest sanitized = exchange.getRequest().mutate()
                .headers(h -> TRUSTED_HEADERS.forEach(h::remove))
                .build();
        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitized).build();

        // 2. Public paths get the sanitized request and nothing else.
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(sanitizedExchange);
        }

        // 3. Everything else requires a valid JWT.
        String authHeader = sanitized.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("### JWT AUTH 401: no Authorization header for {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String token = authHeader.substring(7);

            // Debug: log token header + length
            log.debug("### Token length={} first20={}",
                    token.length(), token.substring(0, Math.min(20, token.length())));

            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            log.debug("### JWT verified OK. sub={} org_id={} role={}",
                    claims.getSubject(), claims.get("org_id"), claims.get("role"));

            Object role = claims.get("role");

            if (path.startsWith("/api/v1/admin/")) {
                if (role == null || !"ADMIN".equals(role.toString())) {
                    log.warn("### JWT AUTH 403: non-ADMIN role={} for {}", role, path);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            ServerHttpRequest.Builder mutated = sanitized.mutate();

            // ---- identity headers (from JWT only) ----
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

            // ---- SaaS tenant header (from JWT only) ----
            // Claim name must match what auth-service issues. Default: org_id
            String orgId = claims.get("org_id", String.class);
            if (orgId != null && !orgId.isBlank()) {
                mutated.header("X-Org-Id", orgId);
            }

            log.debug("### Forwarding to downstream: X-Org-Id={} X-User-Role={} path={}",
                    orgId, role, path);

            return chain.filter(exchange.mutate().request(mutated.build()).build());

        } catch (Exception e) {
            // ---- Detailed failure log ----
            log.error("### JWT AUTH FAILED for path {}", path);
            log.error("###   Exception: {}", e.getClass().getName());
            log.error("###   Message:   {}", e.getMessage());
            log.error("###   Secret prefix=[{}] length={}",
                    secret == null ? "NULL" : secret.substring(0, Math.min(20, secret.length())),
                    secret == null ? 0 : secret.length());
            log.error("###   Stack trace:", e);

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}