package com.backend.global.filter;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.global.error.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to enforce HTTPS connections.
 * Rejects HTTP requests in production to ensure tokens are only transmitted securely.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpsEnforcementFilter extends OncePerRequestFilter {

    private static final String ERROR_RESPONSE_FORMAT = "{\"code\":\"%s\",\"message\":\"%s\"}";

    private final boolean httpsRequired;

    public HttpsEnforcementFilter(
            @Value("${security.https-required:true}") boolean httpsRequired) {
        this.httpsRequired = httpsRequired;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (httpsRequired && !isSecureRequest(request)) {

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(ERROR_RESPONSE_FORMAT.formatted(
                    ErrorCode.HTTPS_REQUIRED.getCode(),
                    ErrorCode.HTTPS_REQUIRED.getMessage()
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        // Check if request is HTTPS
        if (request.isSecure()) {
            return true;
        }

        // Check X-Forwarded-Proto header (for reverse proxy/load balancer)
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if ("https".equalsIgnoreCase(forwardedProto)) {
            return true;
        }

        // Check X-Forwarded-Ssl header
        String forwardedSsl = request.getHeader("X-Forwarded-Ssl");
        return "on".equalsIgnoreCase(forwardedSsl);
    }
}
