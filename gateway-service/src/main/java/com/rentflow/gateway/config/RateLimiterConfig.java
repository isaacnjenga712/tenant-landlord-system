
package com.rentflow.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    private final Environment env;

    public RateLimiterConfig(Environment env) {
        this.env = env;
    }

    private boolean isLoadTestMode() {
        return Boolean.parseBoolean(env.getProperty("LOAD_TEST_MODE", "false"));
    }

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                        exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                .defaultIfEmpty("anonymous");
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown"
        );
    }

    /**
     * Default rate limiter for most routes.
     * Production: 20 req/s sustained, 40 burst per user.
     * Load test mode: effectively unlimited to allow throughput measurement.
     */
    @Bean
    @Primary
    public RedisRateLimiter defaultRateLimiter() {
        if (isLoadTestMode()) {
            // 10,000 req/s sustained, 20,000 burst — effectively unlimited for load tests
            return new RedisRateLimiter(10_000, 20_000, 1);
        }
        return new RedisRateLimiter(20, 40, 1);
    }

    /**
     * Stricter limiter for auth endpoints (login, register, refresh).
     * Protects against credential stuffing and token grinding.
     * Load test mode: still tight, because auth is not the load test focus.
     */
    @Bean
    public RedisRateLimiter authRateLimiter() {
        if (isLoadTestMode()) {
            // Allow enough for setup() to log in once per test run
            return new RedisRateLimiter(100, 200, 1);
        }
        return new RedisRateLimiter(5, 10, 1);
    }
}