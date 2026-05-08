package com.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.backend.domain.admin.entity.Admin;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

@DisplayName("쿠키 보안")
class CookieSecurityTest extends IntegrationTestBase {

    @Nested
    @DisplayName("사용자 로그인 쿠키 속성")
    class UserLoginCookies {

        @Test
        @DisplayName("로그인 응답에 accessToken, refreshToken Set-Cookie 포함")
        void loginSetsCookies() throws Exception {
            User user = TestDataFactory.createUserWithAllPermissions(em);

            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + user.getEmail() + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isOk())
                    .andReturn();

            var setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
            assertThat(setCookieHeaders).hasSize(2);

            String accessCookie = setCookieHeaders.stream()
                    .filter(h -> h.startsWith("accessToken="))
                    .findFirst().orElse("");
            String refreshCookie = setCookieHeaders.stream()
                    .filter(h -> h.startsWith("refreshToken="))
                    .findFirst().orElse("");

            assertThat(accessCookie).contains("HttpOnly");
            assertThat(refreshCookie).contains("HttpOnly");
            assertThat(accessCookie).contains("SameSite=Lax");
            assertThat(refreshCookie).contains("SameSite=Lax");
        }

        @Test
        @DisplayName("로그아웃 시 쿠키 maxAge=0으로 삭제")
        void logoutClearsCookies() throws Exception {
            User user = TestDataFactory.createUserWithAllPermissions(em);

            // Login first to get cookies
            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + user.getEmail() + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isOk())
                    .andReturn();

            var loginCookies = loginResult.getResponse().getCookies();
            var accessCookie = java.util.Arrays.stream(loginCookies)
                    .filter(c -> "accessToken".equals(c.getName()))
                    .findFirst().orElse(null);
            var refreshCookie = java.util.Arrays.stream(loginCookies)
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .findFirst().orElse(null);

            assertThat(accessCookie).isNotNull();
            assertThat(refreshCookie).isNotNull();

            // Logout
            MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(accessCookie, refreshCookie))
                    .andExpect(status().isOk())
                    .andReturn();

            var logoutCookieHeaders = logoutResult.getResponse().getHeaders("Set-Cookie");

            String clearedAccess = logoutCookieHeaders.stream()
                    .filter(h -> h.startsWith("accessToken="))
                    .findFirst().orElse("");
            String clearedRefresh = logoutCookieHeaders.stream()
                    .filter(h -> h.startsWith("refreshToken="))
                    .findFirst().orElse("");

            assertThat(clearedAccess).contains("Max-Age=0");
            assertThat(clearedRefresh).contains("Max-Age=0");
        }
    }

    @Nested
    @DisplayName("관리자 쿠키 분리")
    class AdminCookies {

        @Test
        @DisplayName("관리자 로그인 시 adminAccessToken이 아닌 tempToken 쿠키가 설정됨 (OTP 단계)")
        void adminLoginSetsTempTokenCookie() throws Exception {
            Admin admin = TestDataFactory.createAdmin(em);

            MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"loginId\":\"" + admin.getLoginId() + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isOk())
                    .andReturn();

            var setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
            // Admin login returns tempToken (OTP step)
            String tempCookie = setCookieHeaders.stream()
                    .filter(h -> h.startsWith("tempToken="))
                    .findFirst().orElse("");

            assertThat(tempCookie).isNotEmpty();
            assertThat(tempCookie).contains("HttpOnly");
        }
    }
}
