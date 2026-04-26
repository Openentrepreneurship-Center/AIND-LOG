package com.backend.domain.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.backend.domain.cart.dto.request.AddToCartRequest;
import com.backend.domain.product.entity.Product;
import com.backend.domain.user.entity.PermissionType;
import com.backend.domain.user.entity.User;
import com.backend.global.common.ItemType;
import com.backend.support.IntegrationTestBase;
import com.backend.support.TestDataFactory;

import jakarta.servlet.http.Cookie;

@DisplayName("Cart API 통합 테스트")
class CartServiceTest extends IntegrationTestBase {

    private Cookie userCookieWithProductPermission() {
        User user = TestDataFactory.createUserWithPermissions(em,
                Set.of(PermissionType.PRODUCT_PURCHASE));
        return userAccessTokenCookie(user);
    }

    private Product createProduct() {
        Product product = Product.builder()
                .name("장바구니 테스트 상품")
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

    @Nested
    @DisplayName("GET /api/v1/carts - 장바구니 조회")
    class GetCart {

        @Test
        @DisplayName("빈 장바구니 조회 성공")
        void getEmptyCart() throws Exception {
            Cookie cookie = userCookieWithProductPermission();

            mockMvc.perform(get("/api/v1/carts").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("인증 없이 장바구니 조회 시 401")
        void getCartWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/carts"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("권한 없이 장바구니 조회 시 403")
        void getCartWithoutPermission() throws Exception {
            User user = TestDataFactory.createUser(em);
            Cookie cookie = userAccessTokenCookie(user);

            mockMvc.perform(get("/api/v1/carts").cookie(cookie))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/carts/items - 장바구니 상품 추가")
    class AddToCart {

        @Test
        @DisplayName("장바구니에 상품 추가 성공")
        void addToCartSuccess() throws Exception {
            Cookie cookie = userCookieWithProductPermission();
            Product product = createProduct();

            AddToCartRequest request = new AddToCartRequest(
                    ItemType.PRODUCT, product.getId(), 1);

            mockMvc.perform(post("/api/v1/carts/items")
                            .cookie(cookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/carts - 장바구니 비우기")
    class ClearCart {

        @Test
        @DisplayName("장바구니 비우기 성공")
        void clearCartSuccess() throws Exception {
            Cookie cookie = userCookieWithProductPermission();

            mockMvc.perform(delete("/api/v1/carts").cookie(cookie))
                    .andExpect(status().isOk());
        }
    }
}
