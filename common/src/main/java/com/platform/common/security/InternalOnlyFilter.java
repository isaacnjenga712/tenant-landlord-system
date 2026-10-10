package com.platform.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Blocks any request to /api/v1/internal/** that lacks a valid
 * X-Internal-Call header matching one of the allowed service names.
 *
 * Ensures that even if a service port is exposed, external callers
 * cannot reach the internal aggregation API.
 */
public class InternalOnlyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Call";
    private final Set<String> allowedCallers;

    public InternalOnlyFilter(Set<String> allowedCallers) {
        this.allowedCallers = allowedCallers;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String caller = request.getHeader(HEADER);
        if (caller == null || !allowedCallers.contains(caller)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":403,\"error\":\"Forbidden\"," +
                    "\"message\":\"Internal endpoint — external callers blocked\"}"
            );
            return;
        }
        chain.doFilter(request, response);
    }
}
