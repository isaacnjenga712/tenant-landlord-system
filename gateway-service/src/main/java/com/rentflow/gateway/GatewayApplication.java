package com.rentflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth",         r -> r.path("/api/auth/**").uri("lb://AUTH-SERVICE"))
            .route("lease",        r -> r.path("/api/leases/**").uri("lb://LEASE-SERVICE"))
            .route("property",     r -> r.path("/api/properties/**").uri("lb://PROPERTY-MANAGEMENT-SERVICE"))
            .route("maintenance",  r -> r.path("/api/tickets/**").uri("lb://MAINTENANCE-TICKET-SERVICE"))
            .route("payment",      r -> r.path("/api/payments/**").uri("lb://PAYMENT-SERVICE"))
            .route("mpesa",        r -> r.path("/api/mpesa/**").uri("lb://MPESA-SERVICE"))
            .route("notification", r -> r.path("/api/notifications/**").uri("lb://NOTIFICATION-ENGINE"))
            .build();
    }
}