package com.property.admin.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestIdForwardingInterceptor() {
        return template -> {
            // 1. Propagate request ID for tracing
            String requestId = MDC.get("requestId");
            if (requestId != null && !requestId.isBlank()) {
                template.header("X-Request-ID", requestId);
            }
            template.header("X-Internal-Call", "admin-service");

            // 2. Propagate tenant + user identity from the inbound HTTP request
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                forward(req, template, "X-Org-Id");
                forward(req, template, "X-User-Id");
                forward(req, template, "X-User-PublicId");
                forward(req, template, "X-User-Role");
            }
        };
    }

    private static void forward(HttpServletRequest req, RequestTemplate template, String header) {
        String value = req.getHeader(header);
        if (value != null && !value.isBlank()) {
            template.header(header, value);
        }
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100L, 1000L, 3);
    }
}
