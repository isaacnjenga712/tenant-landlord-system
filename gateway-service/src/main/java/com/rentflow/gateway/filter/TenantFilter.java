package com.rentflow.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

@Component
public class TenantFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    public static final String TENANT_HEADER = "X-Tenant-ID";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator/",
            "/api/payments/callback"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return chain.filter(exchange);
        }

        String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = exchange.getRequest().getHeaders().getFirst(TENANT_HEADER);
        }

        if (tenantId == null || tenantId.isBlank()) {
            return writeError(exchange, HttpStatus.BAD_REQUEST, "Bad Request",
                    "Missing X-Tenant-ID header", path);
        }

        if (!isValidTenantId(tenantId)) {
            return writeError(exchange, HttpStatus.BAD_REQUEST, "Bad Request",
                    "Invalid X-Tenant-ID format", path);
        }

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(TENANT_HEADER, tenantId)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isValidTenantId(String tenantId) {
        if (tenantId == null || tenantId.length() > 64) {
            return false;
        }
        return tenantId.matches("^[a-zA-Z0-9_-]+$");
    }

    private Mono<Void> writeError(ServerWebExchange exchange,
                                  HttpStatus status,
                                  String error,
                                  String message,
                                  String path) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"status\":" + status.value()
                + ",\"error\":\"" + error
                + "\",\"message\":\"" + message
                + "\",\"path\":\"" + path + "\"}";

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

