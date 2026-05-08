package com.backend.global.filter;

import java.util.List;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 경로 기반 필터링을 제공하는 추상 필터 클래스.
 * 특정 경로를 제외하고 필터를 적용하는 기능을 공통으로 제공한다.
 */
public abstract class AbstractPathMatchingFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 주어진 경로 목록과 요청 경로를 비교하여 필터를 스킵해야 하는지 판단한다.
     *
     * @param request 현재 HTTP 요청
     * @param excludedPaths 제외할 경로 패턴 목록
     * @return 필터를 스킵해야 하면 true, 아니면 false
     */
    protected boolean shouldSkipPath(HttpServletRequest request, List<String> excludedPaths) {
        String path = request.getRequestURI();
        return excludedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
