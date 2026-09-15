package com.rentflow.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Request-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        final String finalRequestId = requestId;

        // Add to downstream request
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER, finalRequestId)
                .build();

        // Add to response
        exchange.getResponse().getHeaders().add(HEADER, finalRequestId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -3;
    }
}
