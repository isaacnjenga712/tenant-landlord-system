package com.property.admin.config;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerConfigCustomizer authCircuitBreakerCustomizer() {
        return CircuitBreakerConfigCustomizer.of("authCB",
                builder -> builder.minimumNumberOfCalls(10));
    }
}
