package com.backend.domain.cart.entity;

import java.math.BigDecimal;

import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.product.entity.Product;
import com.backend.global.common.ItemType;
import com.backend.global.util.UlidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_items_cart_id", columnList = "cart_id")
})
public class CartItem {

    @Id
    @Column(length = 26)
    private String id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_product_id")
    private CustomProduct customProduct;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public CartItem(ItemType itemType, Product product, CustomProduct customProduct, Integer quantity) {
        this.id = UlidGenerator.generate();
        this.itemType = itemType;
        this.product = product;
        this.customProduct = customProduct;
        this.quantity = quantity;
    }

    public static CartItem createProductItem(Product product, Integer quantity) {
        return CartItem.builder()
                .itemType(ItemType.PRODUCT)
                .product(product)
                .quantity(quantity)
                .build();
    }

    public static CartItem createCustomProductItem(CustomProduct customProduct, Integer quantity) {
        return CartItem.builder()
                .itemType(ItemType.CUSTOM_PRODUCT)
                .customProduct(customProduct)
                .quantity(quantity)
                .build();
    }

    public String getItemId() {
        return switch (itemType) {
            case PRODUCT -> product != null ? product.getId() : null;
            case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getId() : null;
        };
    }

    public BigDecimal getUnitPrice() {
        return switch (itemType) {
            case PRODUCT -> product != null ? product.getPrice() : BigDecimal.ZERO;
            case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getApprovedPrice() : BigDecimal.ZERO;
        };
    }

    public String getName() {
        return switch (itemType) {
            case PRODUCT -> product != null ? product.getName() : null;
            case CUSTOM_PRODUCT -> customProduct != null
                    ? customProduct.getName() + "(" + (customProduct.getPet() != null ? customProduct.getPet().getName() : "") + ")"
                    : null;
        };
    }

    public String getImageUrl() {
        return switch (itemType) {
            case PRODUCT -> product != null ? product.getImageUrl() : null;
            case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getImageUrl() : null;
        };
    }

    public BigDecimal getSubtotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public void incrementQuantity(int amount) {
        this.quantity += amount;
    }

    public boolean isProduct() {
        return this.itemType == ItemType.PRODUCT;
    }

    public boolean isCustomProduct() {
        return this.itemType == ItemType.CUSTOM_PRODUCT;
    }
}
