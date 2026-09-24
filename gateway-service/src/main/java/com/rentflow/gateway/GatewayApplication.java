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

            // ---------- Public auth endpoints (login, register, refresh) ----------
            .route("auth", r -> r
                .path("/api/v1/auth/**")
                .uri("lb://AUTH-SERVICE"))

            // ---------- Users ----------
            .route("users", r -> r
                .path("/api/v1/users/**")
                .uri("lb://AUTH-SERVICE"))

            // ---------- Properties ----------
            .route("property", r -> r
                .path("/api/v1/properties/**")
                .uri("lb://PROPERTY-MANAGEMENT-SERVICE"))

            // ---------- Leases ----------
            .route("lease", r -> r
                .path("/api/v1/leases/**")
                .uri("lb://LEASE-SERVICE"))

            // ---------- Payments (rent, history) ----------
            .route("payment", r -> r
                .path("/api/v1/payments/**")
                .uri("lb://PAYMENT-SERVICE"))

            // ---------- M-Pesa STK push (if separate service) ----------
            .route("mpesa", r -> r
                .path("/api/v1/mpesa/**")
                .uri("lb://MPESA-SERVICE"))

            // ---------- Maintenance tickets ----------
            .route("maintenance", r -> r
                .path("/api/v1/maintenance/**")
                .uri("lb://MAINTENANCE-TICKET-SERVICE"))

            // ---------- Notifications ----------
            .route("notification", r -> r
                .path("/api/v1/notifications/**")
                .uri("lb://NOTIFICATION-ENGINE"))

            .build();
    }
}