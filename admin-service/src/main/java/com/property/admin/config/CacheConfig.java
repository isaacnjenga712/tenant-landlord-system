package com.property.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper mapper) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(30))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheCustomizer() {
        return builder -> builder
                .withCacheConfiguration("adminOverview", RedisCacheConfiguration
                        .defaultCacheConfig().entryTtl(Duration.ofSeconds(15)))
                .withCacheConfiguration("financeSummary", RedisCacheConfiguration
                        .defaultCacheConfig().entryTtl(Duration.ofSeconds(60)))
                .withCacheConfiguration("userList", RedisCacheConfiguration
                        .defaultCacheConfig().entryTtl(Duration.ofSeconds(20)));
    }
}
