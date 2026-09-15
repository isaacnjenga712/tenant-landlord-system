package com.rentflow.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Blacklist a token ID for the given TTL.
     * TTL should match the token's remaining validity — after that,
     * the token would be rejected anyway by signature/expiry checks.
     */
    public Mono<Boolean> blacklist(String tokenId, Duration ttl) {
        if (tokenId == null || tokenId.isBlank()) {
            return Mono.just(false);
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return Mono.just(false);
        }

        String key = KEY_PREFIX + tokenId;
        return redisTemplate.opsForValue()
                .set(key, "revoked", ttl)
                .doOnSuccess(ok -> log.info("Token blacklisted: jti={} ttl={}s",
                        tokenId, ttl.getSeconds()))
                .doOnError(e -> log.error("Failed to blacklist token jti={}: {}",
                        tokenId, e.getMessage()))
                .defaultIfEmpty(false);
    }

    /**
     * Returns true if the given token ID is revoked.
     */
    public Mono<Boolean> isBlacklisted(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return Mono.just(false);
        }
        return redisTemplate.hasKey(KEY_PREFIX + tokenId)
                .defaultIfEmpty(false)
                .doOnNext(hit -> {
                    if (Boolean.TRUE.equals(hit)) {
                        log.debug("Blacklist hit: jti={}", tokenId);
                    }
                });
    }

    /**
     * Manually remove a token from the blacklist.
     * Useful for testing or "undo logout" edge cases.
     */
    public Mono<Boolean> unblacklist(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return Mono.just(false);
        }
        return redisTemplate.delete(KEY_PREFIX + tokenId)
                .map(count -> count > 0)
                .defaultIfEmpty(false);
    }

    /**
     * Count of currently blacklisted tokens.
     * Handy for monitoring.
     */
    public Mono<Long> count() {
        return redisTemplate.keys(KEY_PREFIX + "*")
                .count();
    }
}
