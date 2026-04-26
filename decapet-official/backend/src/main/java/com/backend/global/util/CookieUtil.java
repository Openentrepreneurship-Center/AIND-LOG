package com.backend.global.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.backend.domain.auth.dto.internal.TokenPairInfo;
import com.backend.global.config.CookieConfig;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final CookieConfig cookieConfig;

    public ResponseCookie createAccessTokenCookie(String token) {
        return createCookie(
                cookieConfig.getAccessTokenName(),
                token,
                cookieConfig.getAccessTokenPath(),
                cookieConfig.getAccessTokenMaxAge()
        );
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createCookie(
                cookieConfig.getRefreshTokenName(),
                token,
                cookieConfig.getRefreshTokenPath(),
                cookieConfig.getRefreshTokenMaxAge()
        );
    }

    public ResponseCookie createAccessTokenClearCookie() {
        return createClearCookie(
                cookieConfig.getAccessTokenName(),
                cookieConfig.getAccessTokenPath()
        );
    }

    public ResponseCookie createRefreshTokenClearCookie() {
        return createClearCookie(
                cookieConfig.getRefreshTokenName(),
                cookieConfig.getRefreshTokenPath()
        );
    }

    // ===== Temp Token (OTP 인증용) =====

    public ResponseCookie createTempTokenCookie(String token) {
        return createCookie(
                cookieConfig.getTempTokenName(),
                token,
                cookieConfig.getTempTokenPath(),
                cookieConfig.getTempTokenMaxAge()
        );
    }

    public ResponseCookie createTempTokenClearCookie() {
        return createClearCookie(
                cookieConfig.getTempTokenName(),
                cookieConfig.getTempTokenPath()
        );
    }

    public String extractTempToken(HttpServletRequest request) {
        return extractCookie(request, cookieConfig.getTempTokenName());
    }

    // ===== Admin-specific cookie methods =====

    public ResponseCookie createAdminAccessTokenCookie(String token) {
        return createCookie(
                cookieConfig.getAdminAccessTokenName(),
                token,
                cookieConfig.getAdminAccessTokenPath(),
                cookieConfig.getAccessTokenMaxAge()
        );
    }

    public ResponseCookie createAdminRefreshTokenCookie(String token) {
        return createCookie(
                cookieConfig.getAdminRefreshTokenName(),
                token,
                cookieConfig.getAdminRefreshTokenPath(),
                cookieConfig.getAdminRefreshTokenMaxAge()
        );
    }

    public ResponseCookie createAdminAccessTokenClearCookie() {
        return createClearCookie(
                cookieConfig.getAdminAccessTokenName(),
                cookieConfig.getAdminAccessTokenPath()
        );
    }

    public ResponseCookie createAdminRefreshTokenClearCookie() {
        return createClearCookie(
                cookieConfig.getAdminRefreshTokenName(),
                cookieConfig.getAdminRefreshTokenPath()
        );
    }

    public String extractAdminAccessToken(HttpServletRequest request) {
        return extractCookie(request, cookieConfig.getAdminAccessTokenName());
    }

    public String extractAdminRefreshToken(HttpServletRequest request) {
        return extractCookie(request, cookieConfig.getAdminRefreshTokenName());
    }

    public HttpHeaders createAdminTokenHeaders(TokenPairInfo tokenPair) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, createAdminAccessTokenCookie(tokenPair.accessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, createAdminRefreshTokenCookie(tokenPair.refreshToken()).toString());
        return headers;
    }

    public HttpHeaders createAdminTokenClearHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, createAdminAccessTokenClearCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, createAdminRefreshTokenClearCookie().toString());
        return headers;
    }

    public String extractAccessToken(HttpServletRequest request) {
        return extractCookie(request, cookieConfig.getAccessTokenName());
    }

    public String extractRefreshToken(HttpServletRequest request) {
        return extractCookie(request, cookieConfig.getRefreshTokenName());
    }

    public HttpHeaders createTokenHeaders(TokenPairInfo tokenPair) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, createAccessTokenCookie(tokenPair.accessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokenPair.refreshToken()).toString());
        return headers;
    }

    public HttpHeaders createTokenClearHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, createAccessTokenClearCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, createRefreshTokenClearCookie().toString());
        return headers;
    }

    private ResponseCookie createCookie(String name, String value, String path, int maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .path(path)
                .maxAge(maxAge)
                .sameSite(cookieConfig.getSameSite());

        if (cookieConfig.getDomain() != null && !cookieConfig.getDomain().isEmpty()) {
            builder.domain(cookieConfig.getDomain());
        }

        return builder.build();
    }

    private ResponseCookie createClearCookie(String name, String path) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .path(path)
                .maxAge(0)
                .sameSite(cookieConfig.getSameSite());

        if (cookieConfig.getDomain() != null && !cookieConfig.getDomain().isEmpty()) {
            builder.domain(cookieConfig.getDomain());
        }

        return builder.build();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
