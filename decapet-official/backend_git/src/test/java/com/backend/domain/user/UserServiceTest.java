package com.backend.domain.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.backend.domain.user.dto.request.UpdateProfileRequest;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

@DisplayName("UserController 통합 테스트")
class UserServiceTest extends IntegrationTestBase {

    @Nested
    @DisplayName("GET /api/v1/users/me")
    class GetMe {

        @Test
        @DisplayName("인증된 사용자가 조회하면 200과 사용자 정보를 반환한다")
        void getMeSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);

            mockMvc.perform(get("/api/v1/users/me")
                            .cookie(userAccessTokenCookie(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(user.getId()))
                    .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                    .andExpect(jsonPath("$.data.name").value(user.getName()));
        }

        @Test
        @DisplayName("토큰 없이 조회하면 401을 반환한다")
        void getMeUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/me")
    class UpdateProfile {

        @Test
        @DisplayName("이름을 변경하면 200과 변경된 사용자 정보를 반환한다")
        void updateNameSuccess() throws Exception {
            User user = TestDataFactory.createUser(em);

            UpdateProfileRequest request = new UpdateProfileRequest(
                    "새이름", null, null, null, null);

            mockMvc.perform(patch("/api/v1/users/me")
                            .cookie(userAccessTokenCookie(user))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("새이름"));
        }
    }
}
