package com.backend.domain.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.domain.product.entity.Product;
import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

import jakarta.servlet.http.Cookie;

@DisplayName("Product API 통합 테스트")
class ProductServiceTest extends IntegrationTestBase {

    private Product createProduct(String name) {
        Product product = Product.builder()
                .name(name)
                .description("테스트 상품 설명")
                .basePrice(new BigDecimal("15000.00"))
                .price(new BigDecimal("12000.00"))
                .stockQuantity(100)
                .expirationDate(LocalDate.of(2027, 12, 31))
                .imageUrl("https://example.com/image.jpg")
                .allowMultiple(true)
                .build();
        em.persist(product);
        em.flush();
        return product;
    }

    private Cookie userCookieWithProductPermission() {
        User user = TestDataFactory.createUserWithPermissions(em,
                Set.of(PermissionType.PRODUCT_PURCHASE));
        return userAccessTokenCookie(user);
    }

    @Nested
    @DisplayName("GET /api/v1/products/featured - 추천 상품 조회 (공개)")
    class GetFeaturedProducts {

        @Test
        @DisplayName("인증 없이 추천 상품 조회 성공")
        void getFeaturedProductsWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/products/featured"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products - 상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("PRODUCT_PURCHASE 권한으로 상품 목록 조회 성공")
        void getProductsWithPermission() throws Exception {
            Cookie cookie = userCookieWithProductPermission();
            createProduct("테스트 상품");

            mockMvc.perform(get("/api/v1/products").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("권한 없이 상품 목록 조회 시 403")
        void getProductsWithoutPermission() throws Exception {
            User user = TestDataFactory.createUser(em);
            Cookie cookie = userAccessTokenCookie(user);

            mockMvc.perform(get("/api/v1/products").cookie(cookie))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 상품 목록 조회 시 401")
        void getProductsWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id} - 상품 상세 조회")
    class GetProduct {

        @Test
        @DisplayName("상품 상세 조회 성공")
        void getProductSuccess() throws Exception {
            Cookie cookie = userCookieWithProductPermission();
            Product product = createProduct("상세 조회 테스트 상품");

            mockMvc.perform(get("/api/v1/products/{productId}", product.getId())
                            .cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("상세 조회 테스트 상품"));
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 404")
        void getProductNotFound() throws Exception {
            Cookie cookie = userCookieWithProductPermission();

            mockMvc.perform(get("/api/v1/products/{productId}", "nonexistent-id")
                            .cookie(cookie))
                    .andExpect(status().isNotFound());
        }
    }
}
