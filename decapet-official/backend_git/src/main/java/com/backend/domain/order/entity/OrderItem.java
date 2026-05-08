package com.backend.domain.order.entity;

import java.math.BigDecimal;

import com.backend.domain.cart.entity.CartItem;
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
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order_id", columnList = "order_id")
})
public class OrderItem {

    @Id
    @Column(length = 26)
    private String id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType itemType;

    @Column(nullable = false, length = 26)
    private String itemId;

    @Column(nullable = false, length = 255)
    private String itemName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 3)
    private BigDecimal weight;

    @Column
    private Integer itemQuantity;

    @Column(length = 500)
    private String imageUrl;

    @Builder
    public OrderItem(ItemType itemType, String itemId, String itemName,
            BigDecimal unitPrice, Integer quantity, BigDecimal weight, Integer itemQuantity, String imageUrl) {
        this.id = UlidGenerator.generate();
        this.itemType = itemType;
        this.itemId = itemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.weight = weight;
        this.itemQuantity = itemQuantity;
        this.imageUrl = imageUrl;
    }

    /**
     * CartItem으로부터 OrderItem 스냅샷 생성
     */
    public static OrderItem fromCartItem(CartItem cartItem) {
        BigDecimal weight = null;
        Integer itemQuantity = null;

        if (cartItem.isCustomProduct() && cartItem.getCustomProduct() != null) {
            weight = cartItem.getCustomProduct().getWeight();
            itemQuantity = cartItem.getCustomProduct().getQuantity();
        }

        return OrderItem.builder()
                .itemType(cartItem.getItemType())
                .itemId(cartItem.getItemId())
                .itemName(cartItem.getName())
                .unitPrice(cartItem.getUnitPrice())
                .quantity(cartItem.getQuantity())
                .weight(weight)
                .itemQuantity(itemQuantity)
                .imageUrl(cartItem.getImageUrl())
                .build();
    }

    public boolean isProduct() {
        return this.itemType == ItemType.PRODUCT;
    }

    public boolean isCustomProduct() {
        return this.itemType == ItemType.CUSTOM_PRODUCT;
    }
}
