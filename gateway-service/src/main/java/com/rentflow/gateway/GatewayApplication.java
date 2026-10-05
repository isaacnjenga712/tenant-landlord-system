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

            // ---------- Public auth endpoints ----------
            .route("auth", r -> r
                .path("/api/v1/auth/**")
                .uri("lb://AUTH-SERVICE"))

            // ---------- Users ----------
            .route("users", r -> r
                .path("/api/v1/users/**")
                .uri("lb://AUTH-SERVICE"))

            // ---------- Properties, Landlords, Units ----------
            .route("property-service", r -> r
                .path(
                    "/api/v1/properties/**",
                    "/api/v1/landlords/**",
                    "/api/v1/units/**"
                )
                .uri("lb://PROPERTY-MANAGEMENT-SERVICE"))

            // ---------- Leases ----------
            .route("lease", r -> r
                .path("/api/v1/leases/**")
                .uri("lb://LEASE-SERVICE"))

            // ---------- Payments ----------
            .route("payment", r -> r
                .path("/api/v1/payments/**")
                .uri("lb://PAYMENT-SERVICE"))

            // ---------- Invoices + related billing ----------
            .route("invoices", r -> r
                .path(
                    "/api/v1/invoices/**",
                    "/api/v1/invoice-line-items/**",
                    "/api/v1/security-deposits/**",
                    "/api/v1/deposit-deductions/**",
                    "/api/v1/payment-splits/**",
                    "/api/v1/payment-methods/**",
                    "/api/v1/payment-accounts/**",
                    "/api/v1/audit-logs/**"
                )
                .uri("lb://PAYMENT-SERVICE"))

            // ---------- M-Pesa STK push ----------
            .route("mpesa", r -> r
                .path("/api/v1/mpesa/**")
                .uri("lb://MPESA-SERVICE"))

            // ---------- Maintenance tickets ----------
            .route("maintenance", r -> r
                .path(
                    "/api/v1/maintenance/**",
                    "/api/v1/tickets/**"
                )
                .uri("lb://MAINTENANCE-TICKET-SERVICE"))

            // ---------- Notifications (REST) ----------
            .route("notification", r -> r
                .path("/api/v1/notifications/**")
                .filters(f -> f.dedupeResponseHeader(
                    "Access-Control-Allow-Origin Access-Control-Allow-Credentials",
                    "RETAIN_FIRST"))
                .uri("lb://NOTIFICATION-ENGINE"))

            // ---------- Notifications (WebSocket / SockJS) ----------
            .route("notification-ws", r -> r
                .path(
                    "/ws/notifications",
                    "/ws/notifications/**"
                )
                .filters(f -> f.dedupeResponseHeader(
                    "Access-Control-Allow-Origin Access-Control-Allow-Credentials",
                    "RETAIN_FIRST"))
                .uri("lb://NOTIFICATION-ENGINE"))

            .build();
    }
}