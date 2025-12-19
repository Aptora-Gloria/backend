package com.optimetric.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that adds correlation IDs to all HTTP requests for better log tracing.
 * This helps track requests through the entire system including GitHub API calls and database operations.
 */
@Component
@Order(1)
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Generate or extract correlation ID
        String correlationId = getOrGenerateCorrelationId(request);
        
        // Add to MDC for logging
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        
        // Add to response headers
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        // Log request details
        logRequestStart(request, correlationId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Log request completion
            long duration = System.currentTimeMillis() - startTime;
            logRequestEnd(request, response, correlationId, duration);
            
            // Clean up MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        // Check if correlation ID is provided in request header
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.trim().isEmpty()) {
            // Generate new correlation ID
            correlationId = UUID.randomUUID().toString();
        }
        
        return correlationId;
    }

    private void logRequestStart(HttpServletRequest request, String correlationId) {
        log.info("Request started: {} {} - User: {} - Correlation ID: {}", 
                request.getMethod(), 
                request.getRequestURI(),
                request.getRemoteUser(),
                correlationId);
    }

    private void logRequestEnd(HttpServletRequest request, HttpServletResponse response, 
                              String correlationId, long duration) {
        log.info("Request completed: {} {} - Status: {} - Duration: {}ms - Correlation ID: {}", 
                request.getMethod(), 
                request.getRequestURI(),
                response.getStatus(),
                duration,
                correlationId);
    }
}