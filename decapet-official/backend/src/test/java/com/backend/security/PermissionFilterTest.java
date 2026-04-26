package com.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

import jakarta.servlet.http.Cookie;

@DisplayName("PermissionFilter 권한 검증")
class PermissionFilterTest extends IntegrationTestBase {

    private Cookie cookieWithPermissions(PermissionType... perms) {
        User user = TestDataFactory.createUserWithPermissions(em, Set.of(perms));
        return userAccessTokenCookie(user);
    }

    private Cookie cookieWithNoPermissions() {
        User user = TestDataFactory.createUser(em);
        return userAccessTokenCookie(user);
    }

    @Nested
    @DisplayName("PRODUCT_PURCHASE 권한")
    class ProductPurchase {

        @Test
        @DisplayName("권한 있으면 /api/v1/carts에 접근 가능")
        void withPermission() throws Exception {
            Cookie cookie = cookieWithPermissions(PermissionType.PRODUCT_PURCHASE);
            // GET /api/v1/carts 호출 — 403이 아닌지 확인 (실제 데이터 없어도 200 or 다른 status)
            mockMvc.perform(get("/api/v1/carts").cookie(cookie))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("권한 없으면 /api/v1/carts에 403")
        void withoutPermission() throws Exception {
            Cookie cookie = cookieWithNoPermissions();
            mockMvc.perform(get("/api/v1/carts").cookie(cookie))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U007"));
        }

        @Test
        @DisplayName("권한 없으면 /api/v1/orders에 403")
        void ordersWithoutPermission() throws Exception {
            Cookie cookie = cookieWithNoPermissions();
            mockMvc.perform(get("/api/v1/orders").cookie(cookie))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U007"));
        }

        @Test
        @DisplayName("권한 없으면 /api/v1/products/123에 403")
        void productsWithoutPermission() throws Exception {
            Cookie cookie = cookieWithNoPermissions();
            mockMvc.perform(get("/api/v1/products/some-id").cookie(cookie))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U007"));
        }
    }

    @Nested
    @DisplayName("APPOINTMENT_BOOKING 권한")
    class AppointmentBooking {

        @Test
        @DisplayName("권한 있으면 /api/v1/appointments에 접근 가능")
        void withPermission() throws Exception {
            Cookie cookie = cookieWithPermissions(PermissionType.APPOINTMENT_BOOKING);
            mockMvc.perform(get("/api/v1/appointments").cookie(cookie))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("권한 없으면 /api/v1/appointments에 403")
        void withoutPermission() throws Exception {
            Cookie cookie = cookieWithNoPermissions();
            mockMvc.perform(get("/api/v1/appointments").cookie(cookie))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U008"));
        }

        @Test
        @DisplayName("권한 없으면 /api/v1/medicine-carts에 403")
        void medicineCarts() throws Exception {
            Cookie cookie = cookieWithNoPermissions();
            mockMvc.perform(get("/api/v1/medicine-carts").cookie(cookie))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U008"));
        }

        @Test
        @DisplayName("권한 없으면 /api/v1/schedules에 403")
        void schedules() throws Exception {
            Cookie cookie = cookieWithNoPermissions();
            mockMvc.perform(get("/api/v1/schedules").cookie(cookie))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U008"));
        }
    }

    @Nested
    @DisplayName("REPORT_CENTER 권한")
    class ReportCenter {

        @Test
        @DisplayName("권한 없으면 /api/v1/posts에 403")
        void withoutPermission() throws Exception {
            Cookie cookie = cookieWithNoPermissions();
            mockMvc.perform(post("/api/v1/posts").cookie(cookie))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U010"));
        }

        @Test
        @DisplayName("권한 있으면 /api/v1/posts에 접근 가능 (403이 아닌 것 확인)")
        void withPermission() throws Exception {
            Cookie cookie = cookieWithPermissions(PermissionType.REPORT_CENTER);
            // POST /api/v1/posts requires multipart — 400 or other error expected, but NOT 403
            int status = mockMvc.perform(post("/api/v1/posts").cookie(cookie))
                    .andReturn().getResponse().getStatus();
            // 403이 아니면 PermissionFilter를 통과한 것
            org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(403);
        }
    }

    @Nested
    @DisplayName("PAYMENT 경로 (OR 조건)")
    class PaymentPath {

        @Test
        @DisplayName("PRODUCT_PURCHASE만 있어도 /api/v1/payments 접근 가능")
        void productPurchaseOnly() throws Exception {
            Cookie cookie = cookieWithPermissions(PermissionType.PRODUCT_PURCHASE);
            // payments GET은 200 or 다른 정상 상태 (403이 아닌 것 확인)
            mockMvc.perform(get("/api/v1/payments").cookie(cookie))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("APPOINTMENT_BOOKING만 있어도 /api/v1/payments 접근 가능")
        void appointmentBookingOnly() throws Exception {
            Cookie cookie = cookieWithPermissions(PermissionType.APPOINTMENT_BOOKING);
            mockMvc.perform(get("/api/v1/payments").cookie(cookie))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("둘 다 없으면 /api/v1/payments에 403")
        void neitherPermission() throws Exception {
            Cookie cookie = cookieWithPermissions(PermissionType.REPORT_CENTER);
            mockMvc.perform(get("/api/v1/payments").cookie(cookie))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("U007"));
        }
    }

    @Nested
    @DisplayName("제외 경로 (권한 체크 없이 통과)")
    class ExcludedPaths {

        @Test
        @DisplayName("/api/v1/breeds는 인증 없이도 접근 가능")
        void breedsPublic() throws Exception {
            mockMvc.perform(get("/api/v1/breeds"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/api/v1/products/featured는 인증 없이도 접근 가능")
        void featuredProductsPublic() throws Exception {
            mockMvc.perform(get("/api/v1/products/featured"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("/api/v1/medicines/featured는 인증 없이도 접근 가능")
        void featuredMedicinesPublic() throws Exception {
            mockMvc.perform(get("/api/v1/medicines/featured"))
                    .andExpect(status().isOk());
        }
    }
}
