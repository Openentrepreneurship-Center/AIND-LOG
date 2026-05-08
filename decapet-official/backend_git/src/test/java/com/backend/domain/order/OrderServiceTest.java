package com.backend.domain.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

import jakarta.servlet.http.Cookie;

@DisplayName("Order API 통합 테스트")
class OrderServiceTest extends IntegrationTestBase {

    private Cookie userCookieWithProductPermission() {
        User user = TestDataFactory.createUserWithPermissions(em,
                Set.of(PermissionType.PRODUCT_PURCHASE));
        return userAccessTokenCookie(user);
    }

    @Nested
    @DisplayName("GET /api/v1/orders - 주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("빈 주문 목록 조회 성공")
        void getEmptyOrders() throws Exception {
            Cookie cookie = userCookieWithProductPermission();

            mockMvc.perform(get("/api/v1/orders").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("인증 없이 주문 목록 조회 시 401")
        void getOrdersWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/orders"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("권한 없이 주문 목록 조회 시 403")
        void getOrdersWithoutPermission() throws Exception {
            User user = TestDataFactory.createUser(em);
            Cookie cookie = userAccessTokenCookie(user);

            mockMvc.perform(get("/api/v1/orders").cookie(cookie))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId} - 주문 상세 조회")
    class GetOrder {

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 404")
        void getOrderNotFound() throws Exception {
            Cookie cookie = userCookieWithProductPermission();

            mockMvc.perform(get("/api/v1/orders/{orderId}", "nonexistent-id")
                            .cookie(cookie))
                    .andExpect(status().isNotFound());
        }
    }
}
