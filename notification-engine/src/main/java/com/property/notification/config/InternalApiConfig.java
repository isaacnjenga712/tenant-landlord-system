package com.property.notification.config;

import com.platform.common.security.InternalOnlyFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class InternalApiConfig {

    @Bean
    public FilterRegistrationBean<InternalOnlyFilter> internalOnlyFilter(
            @Value("${internal.allowed-callers:admin-service}") String allowed) {

        FilterRegistrationBean<InternalOnlyFilter> reg =
                new FilterRegistrationBean<>(
                        new InternalOnlyFilter(Set.of(allowed.split(","))));
        reg.addUrlPatterns("/api/v1/internal/*");
        reg.setOrder(1);
        return reg;
    }
}
