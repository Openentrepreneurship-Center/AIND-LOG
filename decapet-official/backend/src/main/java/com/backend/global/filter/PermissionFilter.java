package com.backend.global.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.domain.user.entity.PermissionType;
import com.backend.global.error.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PermissionFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	// 권한 체크 제외 경로 (공개 API)
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
		"/api/v1/users/**",
		"/api/v1/pets/**",
		"/api/v1/products/featured",
		"/api/v1/medicines/featured"
	);

	// PRODUCT_PURCHASE 권한 필요 경로
	private static final List<String> PRODUCT_PURCHASE_PATHS = List.of(
		"/api/v1/carts/**",
		"/api/v1/orders/**",
		"/api/v1/deliveries/**",
		"/api/v1/products/**",
		"/api/v1/custom-products/**"
	);

	// PAYMENT 경로 — PRODUCT_PURCHASE 또는 APPOINTMENT_BOOKING 중 하나 필요
	private static final List<String> PAYMENT_PATHS = List.of(
		"/api/v1/payments/**"
	);

	// APPOINTMENT_BOOKING 권한 필요 경로
	private static final List<String> APPOINTMENT_BOOKING_PATHS = List.of(
		"/api/v1/appointments/**",
		"/api/v1/medicine-carts/**",
		"/api/v1/medicines/**",
		"/api/v1/prescriptions/**",
		"/api/v1/schedules/**"
	);

	// REPORT_CENTER 권한 필요 경로
	private static final List<String> REPORT_CENTER_PATHS = List.of(
		"/api/v1/posts/**"
	);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String path = request.getRequestURI();

		// 제외 경로는 권한 체크 없이 통과
		if (shouldSkip(path)) {
			filterChain.doFilter(request, response);
			return;
		}

		// 인증되지 않은 사용자는 통과 (Security에서 처리)
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
			|| "anonymousUser".equals(authentication.getPrincipal())) {
			filterChain.doFilter(request, response);
			return;
		}

		// 권한 체크
		ErrorCode errorCode = checkPermission(request, path);
		if (errorCode != null) {
			sendErrorResponse(response, errorCode);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean shouldSkip(String path) {
		return EXCLUDED_PATHS.stream()
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	@SuppressWarnings("unchecked")
	private ErrorCode checkPermission(HttpServletRequest request, String path) {
		List<String> permissions = (List<String>)request.getAttribute(JwtFilter.PERMISSIONS_ATTRIBUTE);
		if (permissions == null) {
			permissions = Collections.emptyList();
		}

		// PRODUCT_PURCHASE 체크
		if (matchesAnyPath(path, PRODUCT_PURCHASE_PATHS)) {
			if (!permissions.contains(PermissionType.PRODUCT_PURCHASE.name())) {
				return ErrorCode.PRODUCT_PURCHASE_NOT_ALLOWED;
			}
		}

		// PAYMENT 경로 — PRODUCT_PURCHASE 또는 APPOINTMENT_BOOKING 중 하나 필요
		if (matchesAnyPath(path, PAYMENT_PATHS)) {
			if (!permissions.contains(PermissionType.PRODUCT_PURCHASE.name())
				&& !permissions.contains(PermissionType.APPOINTMENT_BOOKING.name())) {
				return ErrorCode.PRODUCT_PURCHASE_NOT_ALLOWED;
			}
		}

		// APPOINTMENT_BOOKING 체크 (medicines/featured는 제외됨)
		if (matchesAnyPath(path, APPOINTMENT_BOOKING_PATHS)) {
			if (!permissions.contains(PermissionType.APPOINTMENT_BOOKING.name())) {
				return ErrorCode.APPOINTMENT_BOOKING_NOT_ALLOWED;
			}
		}

		// REPORT_CENTER 체크
		if (matchesAnyPath(path, REPORT_CENTER_PATHS)) {
			if (!permissions.contains(PermissionType.REPORT_CENTER.name())) {
				return ErrorCode.REPORT_CENTER_NOT_ALLOWED;
			}
		}

		return null;
	}

	private boolean matchesAnyPath(String path, List<String> patterns) {
		return patterns.stream()
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
		response.setStatus(HttpStatus.FORBIDDEN.value());
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
