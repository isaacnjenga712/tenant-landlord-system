package com.property.admin.config;

import feign.RequestInterceptor;
import feign.Retryer;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestIdForwardingInterceptor() {
        return template -> {
            String requestId = MDC.get("requestId");
            if (requestId != null && !requestId.isBlank()) {
                template.header("X-Request-ID", requestId);
            }
            // Internal calls are trusted within the Docker network.
            template.header("X-Internal-Call", "admin-service");
        };
    }

    @Bean
    public Retryer feignRetryer() {
        // Up to 2 retries with 100ms backoff, cap at 1s
        return new Retryer.Default(100L, 1000L, 3);
    }
}
