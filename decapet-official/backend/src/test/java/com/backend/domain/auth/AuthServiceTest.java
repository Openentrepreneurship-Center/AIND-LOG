package com.backend.domain.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.backend.domain.auth.dto.request.LoginRequest;
import com.backend.domain.auth.entity.RefreshToken;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

import jakarta.servlet.http.Cookie;

@DisplayName("AuthController 통합 테스트")
class AuthServiceTest extends IntegrationTestBase {

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("올바른 이메일과 비밀번호로 로그인하면 200과 Set-Cookie를 반환한다")
        void loginSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);

            LoginRequest request = new LoginRequest(user.getEmail(), TestDataFactory.DEFAULT_PASSWORD);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("A004"))
                    .andExpect(cookie().exists("accessToken"))
                    .andExpect(cookie().exists("refreshToken"));
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인하면 400 A009를 반환한다")
        void loginWrongPassword() throws Exception {
            User user = TestDataFactory.createUser(em);

            LoginRequest request = new LoginRequest(user.getEmail(), "WrongPassword1!");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("A009"));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인하면 404 U001을 반환한다")
        void loginNonexistentEmail() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent@test.com", TestDataFactory.DEFAULT_PASSWORD);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("U001"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {

        @Test
        @DisplayName("access+refresh 쿠키로 로그아웃하면 200과 쿠키가 삭제된다")
        void logoutSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);

            String rawAccessToken = createRawAccessToken(user);
            String rawRefreshToken = createRawRefreshToken();
            String refreshTokenHash = jwtProvider.hashToken(rawRefreshToken);

            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .userId(user.getId())
                    .tokenHash(refreshTokenHash)
                    .ttlDays(30)
                    .build();
            em.persist(refreshTokenEntity);
            em.flush();

            mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(new Cookie("accessToken", rawAccessToken))
                            .cookie(new Cookie("refreshToken", rawRefreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().maxAge("accessToken", 0))
                    .andExpect(cookie().maxAge("refreshToken", 0));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("유효한 refresh 쿠키로 토큰 갱신하면 200과 새 쿠키를 반환한다")
        void refreshSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);

            String rawRefreshToken = createRawRefreshToken();
            String refreshTokenHash = jwtProvider.hashToken(rawRefreshToken);

            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .userId(user.getId())
                    .tokenHash(refreshTokenHash)
                    .ttlDays(30)
                    .build();
            em.persist(refreshTokenEntity);
            em.flush();

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(new Cookie("refreshToken", rawRefreshToken)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("accessToken"))
                    .andExpect(cookie().exists("refreshToken"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/check-email")
    class CheckEmail {

        @Test
        @DisplayName("사용 가능한 이메일이면 available: true를 반환한다")
        void checkEmailAvailable() throws Exception {
            mockMvc.perform(get("/api/v1/auth/check-email")
                            .param("email", "available@test.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(true));
        }

        @Test
        @DisplayName("이미 등록된 이메일이면 available: false를 반환한다")
        void checkEmailNotAvailable() throws Exception {
            User user = TestDataFactory.createUser(em);

            mockMvc.perform(get("/api/v1/auth/check-email")
                            .param("email", user.getEmail()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(false));
        }
    }
}
