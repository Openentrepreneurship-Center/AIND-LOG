package com.backend.domain.cart.repository;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, String> {

    Optional<Cart> findByUserId(String userId);

    default Cart getOrCreate(String userId, Supplier<Cart> cartSupplier) {
        return findByUserId(userId)
                .orElseGet(() -> save(cartSupplier.get()));
    }

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.product.id = :productId")
    void deleteCartItemsByProductId(@Param("productId") String productId);
}
