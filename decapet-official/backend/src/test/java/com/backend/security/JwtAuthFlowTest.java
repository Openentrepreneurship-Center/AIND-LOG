package com.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.domain.admin.entity.Admin;
import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

@DisplayName("JWT 인증 플로우")
class JwtAuthFlowTest extends IntegrationTestBase {

    private User user;
    private Admin admin;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.createUserWithAllPermissions(em);
        admin = TestDataFactory.createAdmin(em);
    }

    @Nested
    @DisplayName("JWT 토큰 인증")
    class TokenAuthentication {

        @Test
        @DisplayName("유효한 access token이 있으면 보호 엔드포인트에 접근 가능")
        void validTokenAllowsAccess() throws Exception {
            mockMvc.perform(get("/api/v1/users/me")
                            .cookie(userAccessTokenCookie(user)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("토큰이 없으면 보호 엔드포인트에 401 반환")
        void noTokenReturns401() throws Exception {
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("만료된 토큰은 인증 실패")
        void expiredTokenFails() throws Exception {
            // Create an expired token by setting negative expiration
            String expiredToken = Jwts.builder()
                    .subject(user.getId())
                    .claim("role", "USER")
                    .claim("permissions", List.of())
                    .expiration(new java.util.Date(System.currentTimeMillis() - 1000))
                    .signWith(Keys.hmacShaKeyFor(
                            "test-secret-key-that-is-at-least-64-characters-long-for-hmac-sha256-algorithm-testing"
                                    .getBytes()))
                    .compact();

            mockMvc.perform(get("/api/v1/users/me")
                            .cookie(new Cookie("accessToken", expiredToken)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("잘못된 서명의 토큰은 인증 실패")
        void invalidSignatureFails() throws Exception {
            String badToken = Jwts.builder()
                    .subject(user.getId())
                    .claim("role", "USER")
                    .claim("permissions", List.of())
                    .expiration(new java.util.Date(System.currentTimeMillis() + 1800000))
                    .signWith(Keys.hmacShaKeyFor(
                            "a-completely-different-secret-key-that-is-at-least-64-characters-long-for-testing"
                                    .getBytes()))
                    .compact();

            mockMvc.perform(get("/api/v1/users/me")
                            .cookie(new Cookie("accessToken", badToken)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("쿠키 기반 토큰 라우팅")
    class CookieRouting {

        @Test
        @DisplayName("/api/v1/admin/** 경로는 adminAccessToken 쿠키에서 토큰 추출")
        void adminPathUsesAdminCookie() throws Exception {
            // Admin accessing admin endpoint with adminAccessToken cookie
            mockMvc.perform(get("/api/v1/admin/users")
                            .cookie(adminAccessTokenCookie(admin)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("admin 경로에 user 쿠키(accessToken)로 접근 시 인증 실패")
        void adminPathWithUserCookieFails() throws Exception {
            // Put admin token in the wrong cookie name
            String token = jwtProvider.createAccessToken(admin.getId(), "ADMIN", List.of());
            mockMvc.perform(get("/api/v1/admin/users")
                            .cookie(new Cookie("accessToken", token)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("user 경로에 admin 쿠키(adminAccessToken)로 접근 시 인증 실패")
        void userPathWithAdminCookieFails() throws Exception {
            String token = jwtProvider.createAccessToken(user.getId(), "USER",
                    List.of(PermissionType.PRODUCT_PURCHASE.name()));
            mockMvc.perform(get("/api/v1/users/me")
                            .cookie(new Cookie("adminAccessToken", token)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
