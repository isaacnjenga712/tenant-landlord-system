package com.rentflow.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class TenantFilter implements GlobalFilter, Ordered {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip actuator
        if (path.startsWith("/actuator/")) {
            return chain.filter(exchange);
        }

        // Skip the mpesa callback — Safaricom doesn't send tenant headers
        if (path.startsWith("/api/mpesa/") || path.startsWith("/api/payments/callback")) {
            return chain.filter(exchange);
        }

        // The tenant can come from:
        //  1. JWT filter (already set X-Tenant-Id header)
        //  2. Client-supplied X-Tenant-ID header (public endpoints)
        String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = exchange.getRequest().getHeaders().getFirst(TENANT_HEADER);
        }

        if (tenantId == null || tenantId.isBlank()) {
            return badRequest(exchange, "Missing X-Tenant-ID header");
        }

        // Validate format (UUID or alphanumeric)
        if (!isValidTenantId(tenantId)) {
            return badRequest(exchange, "Invalid X-Tenant-ID format");
        }

        // Ensure the header is set downstream with the canonical name
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(TENANT_HEADER, tenantId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isValidTenantId(String tenantId) {
        // Accept UUIDs or alphanumeric IDs between 1 and 64 chars
        if (tenantId.length() > 64) return false;
        return tenantId.matches("^[a-zA-Z0-9_-]+$");
    }

    private Mono<Void> badRequest(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"%s\",\"path\":\"%s\"}",
                message, exchange.getRequest().getURI().getPath());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}