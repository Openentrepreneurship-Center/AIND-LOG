package com.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.backend.domain.admin.entity.Admin;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

@DisplayName("계정 잠금")
class AccountLockingTest extends IntegrationTestBase {

    @Nested
    @DisplayName("사용자 계정 잠금")
    class UserLocking {

        @Test
        @DisplayName("5회 연속 로그인 실패 시 계정 잠금")
        void lockAfter5Failures() throws Exception {
            User user = TestDataFactory.createUserWithAllPermissions(em);
            String email = user.getEmail();

            // 5회 실패
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"WrongPass123!\"}"));
            }

            // 6번째: 올바른 비밀번호로도 잠금 상태
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U012"));
        }

        @Test
        @DisplayName("4회 실패는 잠금되지 않음")
        void noLockAfter4Failures() throws Exception {
            User user = TestDataFactory.createUserWithAllPermissions(em);
            String email = user.getEmail();

            for (int i = 0; i < 4; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"WrongPass123!\"}"));
            }

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("로그인 성공 시 실패 횟수 초기화")
        void successResetsCounter() throws Exception {
            User user = TestDataFactory.createUserWithAllPermissions(em);
            String email = user.getEmail();

            // 3회 실패
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"WrongPass123!\"}"));
            }

            // 성공 → 카운터 리셋
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isOk());

            // 다시 3회 실패 (총 6회이지만 리셋됨)
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"WrongPass123!\"}"));
            }

            // 여전히 잠금 아님
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("잠금 시간 경과 후 로그인 가능")
        void unlockAfterDuration() throws Exception {
            User user = TestDataFactory.createUserWithAllPermissions(em);
            String email = user.getEmail();

            // 5회 실패 → 잠금
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"WrongPass123!\"}"));
            }

            // lockedUntil을 과거로 변경
            em.createQuery("UPDATE User u SET u.lockedUntil = :past WHERE u.id = :id")
                    .setParameter("past", LocalDateTime.now().minusMinutes(1))
                    .setParameter("id", user.getId())
                    .executeUpdate();
            em.flush();
            em.clear();

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("관리자 계정 잠금")
    class AdminLocking {

        @Test
        @DisplayName("5회 연속 실패 시 관리자 계정 잠금")
        void lockAfter5Failures() throws Exception {
            Admin admin = TestDataFactory.createAdmin(em);

            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"" + admin.getLoginId() + "\",\"password\":\"WrongPass123!\"}"));
            }

            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"loginId\":\"" + admin.getLoginId() + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("AD006"));
        }

        @Test
        @DisplayName("관리자 잠금 시간 경과 후 로그인 가능")
        void unlockAfterDuration() throws Exception {
            Admin admin = TestDataFactory.createAdmin(em);

            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"" + admin.getLoginId() + "\",\"password\":\"WrongPass123!\"}"));
            }

            em.createQuery("UPDATE Admin a SET a.lockedUntil = :past WHERE a.id = :id")
                    .setParameter("past", LocalDateTime.now().minusMinutes(1))
                    .setParameter("id", admin.getId())
                    .executeUpdate();
            em.flush();
            em.clear();

            // Admin login returns OTP status, not 200 directly - but should not be 403
            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"loginId\":\"" + admin.getLoginId() + "\",\"password\":\"" + TestDataFactory.DEFAULT_PASSWORD + "\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Entity 단위 검증")
    class EntityValidation {

        @Test
        @DisplayName("isLocked - lockedUntil이 미래이면 true")
        void isLockedFuture() {
            User user = TestDataFactory.createUser(em);
            // 5회 실패로 잠금
            for (int i = 0; i < 5; i++) {
                user.recordLoginFailure();
            }
            assertThat(user.isLocked()).isTrue();
        }

        @Test
        @DisplayName("isLocked - lockedUntil이 null이면 false")
        void isLockedNull() {
            User user = TestDataFactory.createUser(em);
            assertThat(user.isLocked()).isFalse();
        }

        @Test
        @DisplayName("resetLoginAttempts 호출 시 잠금 해제")
        void resetClearsLock() {
            User user = TestDataFactory.createUser(em);
            for (int i = 0; i < 5; i++) {
                user.recordLoginFailure();
            }
            assertThat(user.isLocked()).isTrue();

            user.resetLoginAttempts();
            assertThat(user.isLocked()).isFalse();
            assertThat(user.getFailedLoginAttempts()).isZero();
        }
    }
}
