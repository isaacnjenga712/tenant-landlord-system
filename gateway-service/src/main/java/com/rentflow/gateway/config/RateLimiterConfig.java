
package com.rentflow.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

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
     * @Primary so Spring's auto-configured RequestRateLimiterGatewayFilterFactory
     * injects this as the default. Routes can still reference authRateLimiter
     * explicitly via .setRateLimiter(authRateLimiter).
     */
    @Bean
    @Primary
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(20, 40, 1);
    }

    @Bean
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }
}