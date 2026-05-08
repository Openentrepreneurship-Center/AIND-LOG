package com.backend.global.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.backend.domain.admin.repository.AdminRepository;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.error.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * JWT 토큰의 계정이 실제 DB에 존재하는지 검증하는 필터.
 * 서버 재시작 등으로 DB가 초기화된 경우 stale token을 거부한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountValidationFilter extends AbstractPathMatchingFilter {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api/v1/auth/**",
            "/api/v1/admin/auth/**",
            "/api/v1/breeds/**",
            "/api/v1/banners/**",
            "/api/v1/terms/**",
            "/api/v1/products/featured",
            "/api/v1/medicines/featured"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

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

        String userId = (String) authentication.getPrincipal();
        if (userId == null) {
            sendUnauthorizedResponse(response);
            return;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        boolean exists = isAdmin
                ? adminRepository.existsById(userId)
                : userRepository.existsById(userId);

        if (!exists) {
            sendUnauthorizedResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
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
