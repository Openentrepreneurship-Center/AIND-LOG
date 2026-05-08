package com.backend.global.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.domain.auth.repository.TokenBlacklistRepository;
import com.backend.global.security.JwtProvider;
import com.backend.global.util.CookieUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public static final String PERMISSIONS_ATTRIBUTE = "userPermissions";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            String jti = jwtProvider.getJtiFromToken(token);
            if (jti != null && tokenBlacklistRepository.existsByJti(jti)) {
                filterChain.doFilter(request, response);
                return;
            }

            String userId = jwtProvider.getUserIdFromToken(token);
            String role = jwtProvider.getRoleFromToken(token);
            List<String> permissions = jwtProvider.getPermissionsFromToken(token);

            List<GrantedAuthority> authorities = role != null
                    ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    : Collections.emptyList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(PERMISSIONS_ATTRIBUTE, permissions);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/v1/admin/")) {
            return cookieUtil.extractAdminAccessToken(request);
        }
        return cookieUtil.extractAccessToken(request);
    }
}
