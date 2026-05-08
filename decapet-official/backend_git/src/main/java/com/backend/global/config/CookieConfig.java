package com.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * Cookie configuration for JWT token security.
 * Manages HttpOnly, Secure, SameSite cookie settings.
 */
@Getter
@Configuration
public class CookieConfig {

    @Value("${cookie.domain:}")
    private String domain;

    @Value("${cookie.secure:true}")
    private boolean secure;

    @Value("${cookie.same-site:Strict}")
    private String sameSite;

    @Value("${cookie.access-token-name:accessToken}")
    private String accessTokenName;

    @Value("${cookie.refresh-token-name:refreshToken}")
    private String refreshTokenName;

    @Value("${cookie.access-token-path:/}")
    private String accessTokenPath;

    @Value("${cookie.refresh-token-path:/}")
    private String refreshTokenPath;

    @Value("${cookie.admin-access-token-name:adminAccessToken}")
    private String adminAccessTokenName;

    @Value("${cookie.admin-refresh-token-name:adminRefreshToken}")
    private String adminRefreshTokenName;

    @Value("${cookie.admin-access-token-path:/}")
    private String adminAccessTokenPath;

    @Value("${cookie.admin-refresh-token-path:/}")
    private String adminRefreshTokenPath;

    @Value("${cookie.temp-token-name:tempToken}")
    private String tempTokenName;

    @Value("${cookie.temp-token-path:/api/v1/admin/auth}")
    private String tempTokenPath;

    @Value("${jwt.access-expiration:1800000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshTokenExpiration;

    @Value("${jwt.admin-refresh-expiration:604800000}")
    private long adminRefreshTokenExpiration;

    /**
     * Get access token max age in seconds
     */
    public int getAccessTokenMaxAge() {
        return (int) (accessTokenExpiration / 1000);
    }

    /**
     * Get refresh token max age in seconds
     */
    public int getRefreshTokenMaxAge() {
        return (int) (refreshTokenExpiration / 1000);
    }

    /**
     * Get admin refresh token max age in seconds (7 days)
     */
    public int getAdminRefreshTokenMaxAge() {
        return (int) (adminRefreshTokenExpiration / 1000);
    }

    public int getTempTokenMaxAge() {
        return 5 * 60; // 5분
    }

}
