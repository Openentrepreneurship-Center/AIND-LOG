package com.backend.domain.cart.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.domain.user.entity.User;
import com.backend.global.common.ItemType;
import com.backend.global.util.UlidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "carts")
@EntityListeners(AuditingEntityListener.class)
public class Cart {

    @Id
    @Column(length = 26)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Cart(User user) {
        this.id = UlidGenerator.generate();
        this.user = user;
        this.items = new ArrayList<>();
    }

    public void addItem(CartItem item) {
        Optional<CartItem> existingItem = findItem(item.getItemType(), item.getItemId());

        if (existingItem.isPresent()) {
            existingItem.get().incrementQuantity(item.getQuantity());
        } else {
            item.setCart(this);
            items.add(item);
        }
    }

    public void updateItemQuantity(ItemType itemType, String itemId, int quantity) {
        findItem(itemType, itemId).ifPresent(item -> {
            if (quantity <= 0) {
                removeItem(itemType, itemId);
            } else {
                item.updateQuantity(quantity);
            }
        });
    }

    public void removeItem(ItemType itemType, String itemId) {
        items.removeIf(item -> item.getItemType() == itemType && item.getItemId().equals(itemId));
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Optional<CartItem> findItem(ItemType itemType, String itemId) {
        return items.stream()
                .filter(item -> item.getItemType() == itemType && item.getItemId().equals(itemId))
                .findFirst();
    }
}
