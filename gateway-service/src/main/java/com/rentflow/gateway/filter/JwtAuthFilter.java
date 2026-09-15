
package com.rentflow.gateway.filter;

import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.cloud.gateway.filter.GatewayFilter;

import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;



@Component

public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")

    private String secret;

    public JwtAuthFilter() { super(Config.class); }

    @Override

    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            String path = exchange.getRequest().getPath().value();

            if (path.contains("/api/auth/") || path.contains("/callback")) {

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

                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

                var mutated = exchange.getRequest().mutate()

                    .header("X-User-Id", claims.getSubject())

                    .header("X-User-Role", claims.get("role", String.class))

                    .build();

                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (Exception e) {

                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                return exchange.getResponse().setComplete();

            }

        };

    }

    public static class Config {}

}

