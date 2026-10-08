package com.rentflow.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();

            headers.set("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains");
            headers.set("X-Content-Type-Options", "nosniff");
            headers.set("X-Frame-Options", "DENY");
            headers.set("X-XSS-Protection", "1; mode=block");
            headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
            headers.set("Permissions-Policy",
                    "geolocation=(), microphone=(), camera=()");

            return Mono.empty();
        });

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;   // runs before JwtAuthFilter (-1)
    }
}
