package com.rentflow.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantCacheService {

    private static final String KEY_PREFIX = "tenant:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    /**
     * Look up tenant metadata by ID.
     * Returns empty Mono if not cached.
     */
    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.empty();
        }

        return redisTemplate.opsForValue()
                .get(KEY_PREFIX + tenantId)
                .cast(Map.class)
                .map(m -> (Map<String, Object>) m)
                .doOnNext(t -> log.debug("Tenant cache hit: {}", tenantId))
                .doOnError(e -> log.warn("Tenant cache error for {}: {}", tenantId, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Store tenant metadata with the default TTL (15 min).
     */
    public Mono<Boolean> cacheTenant(String tenantId, Map<String, Object> tenant) {
        return cacheTenant(tenantId, tenant, DEFAULT_TTL);
    }

    /**
     * Store tenant metadata with a custom TTL.
     */
    public Mono<Boolean> cacheTenant(String tenantId, Map<String, Object> tenant, Duration ttl) {
        if (tenantId == null || tenantId.isBlank() || tenant == null) {
            return Mono.just(false);
        }

        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + tenantId, tenant, ttl)
                .doOnSuccess(ok -> log.debug("Tenant cached: {} ttl={}s", tenantId, ttl.getSeconds()))
                .defaultIfEmpty(false);
    }

    /**
     * Check whether a tenant exists in cache.
     */
    public Mono<Boolean> exists(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.just(false);
        }
        return redisTemplate.hasKey(KEY_PREFIX + tenantId)
                .defaultIfEmpty(false);
    }

    /**
     * Remove a tenant from cache. Call this when tenant data changes.
     */
    public Mono<Boolean> evict(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.just(false);
        }
        return redisTemplate.delete(KEY_PREFIX + tenantId)
                .map(count -> count > 0)
                .doOnSuccess(ok -> log.info("Tenant cache evicted: {}", tenantId));
    }

    /**
     * Return the remaining TTL of a cached tenant (in seconds).
     * Returns -2 if key doesn't exist, -1 if no TTL set.
     */
    public Mono<Duration> ttl(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.just(Duration.ZERO);
        }
        return redisTemplate.getExpire(KEY_PREFIX + tenantId)
                .defaultIfEmpty(Duration.ZERO)
                .onErrorReturn(Duration.ZERO);
    }
    }

