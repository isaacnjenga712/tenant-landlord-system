package com.apex.auth.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Extracts the tenant ID from the X-Tenant-ID header, stores it in
 * TenantContext, and rejects requests that are missing it.
 *
 * Public endpoints (login, register, refresh, actuator) are exempt —
 * they run before authentication and therefore have no tenant context yet.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    private static final String TENANT_HEADER = "X-Tenant-ID";

    /**
     * Endpoints that do NOT require a tenant context.
     * These run before authentication, so the client cannot know the tenant yet.
     */
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh",
        "/api/v1/auth/callback",
        "/actuator",
        "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean skip = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (skip) {
            log.debug("Skipping tenant filter for public path: {}", path);
        }
        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = request.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.isBlank()) {
            log.warn("Missing {} header for {} {}", TENANT_HEADER,
                    request.getMethod(), request.getRequestURI());
            writeErrorResponse(response, request);
            return;
        }

        try {
            TenantContext.setCurrentTenant(tenantId.trim());
            log.debug("Tenant context set to {} for {} {}",
                    tenantId, request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void writeErrorResponse(HttpServletResponse response, HttpServletRequest request)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = String.format(
            "{\"status\":400,\"error\":\"Bad Request\"," +
            "\"message\":\"Missing %s header\"," +
            "\"path\":\"%s\"}",
            TENANT_HEADER,
            request.getRequestURI()
        );
        response.getWriter().write(body);
    }
}
