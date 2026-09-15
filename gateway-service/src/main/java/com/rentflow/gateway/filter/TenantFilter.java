
package com.rentflow.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;

import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;

import java.util.UUID;



@Component

public class TenantFilter extends AbstractGatewayFilterFactory<TenantFilter.Config> {

    public TenantFilter() { super(Config.class); }

    @Override

    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            String path = exchange.getRequest().getPath().value();

            if (path.contains("/api/auth/") || path.contains("/callback") || path.contains("/actuator")) {

                return chain.filter(exchange);

            }

            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-ID");

            if (tenantId == null || tenantId.isBlank()) {

                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);

                return exchange.getResponse().setComplete();

            }

            var mutated = exchange.getRequest().mutate()

                .header("X-Request-ID", UUID.randomUUID().toString())

                .header("X-Tenant-ID", tenantId)

                .build();

            return chain.filter(exchange.mutate().request(mutated).build());

        };

    }

    public static class Config {}

}

