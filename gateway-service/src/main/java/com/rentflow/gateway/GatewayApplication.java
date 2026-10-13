package com.rentflow.gateway;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    private final RedisRateLimiter authRateLimiter;
    private final RedisRateLimiter defaultRateLimiter;
    private final KeyResolver ipKeyResolver;
    private final KeyResolver userKeyResolver;

    // ---- Route targets ----
    // Default: lb:// (Eureka). Override in application-local.yml with direct URLs.

    @Value("${services.auth.uri:lb://AUTH-SERVICE}")
    private String authUri;

    @Value("${services.property.uri:lb://PROPERTY-MANAGEMENT-SERVICE}")
    private String propertyUri;

    @Value("${services.lease.uri:lb://LEASE-SERVICE}")
    private String leaseUri;

    @Value("${services.payment.uri:lb://PAYMENT-SERVICE}")
    private String paymentUri;

    @Value("${services.mpesa.uri:lb://MPESA-SERVICE}")
    private String mpesaUri;

    @Value("${services.maintenance.uri:lb://MAINTENANCE-TICKET-SERVICE}")
    private String maintenanceUri;

    @Value("${services.notification.uri:lb://NOTIFICATION-ENGINE}")
    private String notificationUri;

    @Value("${services.admin.uri:lb://ADMIN-SERVICE}")
    private String adminUri;

    public GatewayApplication(
            @Qualifier("authRateLimiter") RedisRateLimiter authRateLimiter,
            @Qualifier("defaultRateLimiter") RedisRateLimiter defaultRateLimiter,
            @Qualifier("ipKeyResolver") KeyResolver ipKeyResolver,
            @Qualifier("userKeyResolver") KeyResolver userKeyResolver) {
        this.authRateLimiter = authRateLimiter;
        this.defaultRateLimiter = defaultRateLimiter;
        this.ipKeyResolver = ipKeyResolver;
        this.userKeyResolver = userKeyResolver;
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

            // ---------- Public auth endpoints (strict IP-based limiter) ----------
            .route("auth", r -> r
                .path("/api/v1/auth/**")
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setRateLimiter(authRateLimiter);
                    c.setKeyResolver(ipKeyResolver);
                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                }))
                .uri(authUri))

            // ---------- Users ----------
            .route("users", r -> r
                .path("/api/v1/users/**")
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setRateLimiter(defaultRateLimiter);
                    c.setKeyResolver(userKeyResolver);
                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                }))
                .uri(authUri))

            // ---------- Properties, Landlords, Units ----------
            .route("property-service", r -> r
                .path(
                    "/api/v1/properties/**",
                    "/api/v1/landlords/**",
                    "/api/v1/units/**"
                )
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setRateLimiter(defaultRateLimiter);
                    c.setKeyResolver(userKeyResolver);
                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                }))
                .uri(propertyUri))

            // ---------- Leases ----------
            .route("lease", r -> r
                .path("/api/v1/leases/**")
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setRateLimiter(defaultRateLimiter);
                    c.setKeyResolver(userKeyResolver);
                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                }))
                .uri(leaseUri))

            // ---------- Payments ----------
            .route("payment", r -> r
                .path("/api/v1/payments/**")
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setRateLimiter(defaultRateLimiter);
                    c.setKeyResolver(userKeyResolver);
                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                }))
                .uri(paymentUri))

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
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setRateLimiter(defaultRateLimiter);
                    c.setKeyResolver(userKeyResolver);
                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                }))
                .uri(paymentUri))

            // ---------- M-Pesa STK push ----------
            .route("mpesa", r -> r
                .path("/api/v1/mpesa/**")
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setRateLimiter(defaultRateLimiter);
                    c.setKeyResolver(userKeyResolver);
                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                }))
                .uri(mpesaUri))

            // ---------- Maintenance tickets ----------
            .route("maintenance", r -> r
                .path(
                    "/api/v1/maintenance/**",
                    "/api/v1/tickets/**"
                )
                .filters(f -> f.requestRateLimiter(c -> {
                    c.setRateLimiter(defaultRateLimiter);
                    c.setKeyResolver(userKeyResolver);
                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                }))
                .uri(maintenanceUri))

            // ---------- Notifications (REST) ----------
            .route("notification", r -> r
                .path("/api/v1/notifications/**")
                .filters(f -> f
                    .requestRateLimiter(c -> {
                        c.setRateLimiter(defaultRateLimiter);
                        c.setKeyResolver(userKeyResolver);
                        c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    })
                    .dedupeResponseHeader(
                        "Access-Control-Allow-Origin Access-Control-Allow-Credentials",
                        "RETAIN_FIRST"))
                .uri(notificationUri))

            // ---------- Notifications (WebSocket / SockJS — no rate limit) ----------
            .route("notification-ws", r -> r
                .path(
                    "/ws/notifications",
                    "/ws/notifications/**"
                )
                .filters(f -> f.dedupeResponseHeader(
                    "Access-Control-Allow-Origin Access-Control-Allow-Credentials",
                    "RETAIN_FIRST"))
                .uri(notificationUri))

            // ---------- Admin dashboard ----------
            .route("admin", r -> r
                .path("/api/v1/admin/**")
                .filters(f -> f
                    .requestRateLimiter(c -> {
                        c.setRateLimiter(defaultRateLimiter);
                        c.setKeyResolver(userKeyResolver);
                        c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    })
                    .dedupeResponseHeader(
                        "Access-Control-Allow-Origin Access-Control-Allow-Credentials",
                        "RETAIN_FIRST"))
                .uri(adminUri))

            .build();
    }
}