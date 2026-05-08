package com.backend.global.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.backend.domain.terms.exception.RequiredTermNotAgreedException;
import com.backend.domain.terms.service.TermService;
import com.backend.global.error.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * 일반 사용자(User)의 필수 약관 동의 여부를 검증하는 필터.
 * Admin은 약관 동의 대상이 아니므로 스킵한다.
 */
@Slf4j
@RequiredArgsConstructor
public class TermsConsentFilter extends AbstractPathMatchingFilter {

    private final TermService termService;
    private final ObjectMapper objectMapper;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api/v1/auth/**",
            "/api/v1/admin/**",
            "/api/v1/breeds/**",
            "/api/v1/banners/**",
            "/api/v1/terms/**",
            "/api/v1/products/featured",
            "/api/v1/medicines/featured"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (shouldSkipPath(request, EXCLUDED_PATHS)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Admin은 약관 동의 대상이 아님
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            filterChain.doFilter(request, response);
            return;
        }

        // User만 약관 동의 검증
        String userId = (String) authentication.getPrincipal();
        try {
            termService.validateRequiredTermsConsented(userId);
            filterChain.doFilter(request, response);
        } catch (RequiredTermNotAgreedException e) {
            log.warn("필수 약관 미동의 - userId: {}", userId);
            sendTermsErrorResponse(response);
        }
    }

    private void sendTermsErrorResponse(HttpServletResponse response) throws IOException {
        ErrorCode errorCode = ErrorCode.REQUIRED_TERM_NOT_AGREED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = Map.of(
                "code", errorCode.getCode(),
                "errorMessage", errorCode.getMessage(),
                "httpStatus", errorCode.getStatus().toString()
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
