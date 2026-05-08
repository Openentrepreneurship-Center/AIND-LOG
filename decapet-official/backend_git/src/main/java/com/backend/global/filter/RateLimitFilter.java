package com.backend.global.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.global.config.RateLimitConfig;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;
    private final boolean enabled;

    public RateLimitFilter(RateLimitConfig rateLimitConfig,
                           @Value("${rate-limit.enabled:true}") boolean enabled) {
        this.rateLimitConfig = rateLimitConfig;
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        Bucket bucket;
        if (path.equals("/api/v1/auth/sms/send")) {
            // SMS send: 10/5min - prevents SMS cost attacks
            bucket = rateLimitConfig.resolveSmsVerificationBucket(clientIp);
        } else if (path.equals("/api/v1/auth/sms/verify")) {
            // SMS verify: 15/5min - prevents brute force on verification codes
            bucket = rateLimitConfig.resolveSmsVerifyBucket(clientIp);
        } else if (path.equals("/api/v1/auth/refresh") || path.equals("/api/v1/admin/auth/refresh")) {
            // Token refresh: 5/1min - stricter to prevent abuse
            bucket = rateLimitConfig.resolveRefreshBucket(clientIp);
        } else if (path.startsWith("/api/v1/auth") || path.startsWith("/api/v1/admin/auth")) {
            // Auth endpoints (login, check-email, register): 30/5min
            bucket = rateLimitConfig.resolveAuthBucket(clientIp);
        } else {
            // Global: 1000/5min
            bucket = rateLimitConfig.resolveBucket(clientIp);
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.getWriter().write(
                    "{\"success\":false,\"code\":\"G004\",\"message\":\"Too many requests. Please try again later.\"}"
            );
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
