package com.backend.domain.order.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.backend.domain.order.exception.InvalidOrderStatusException;
import com.backend.domain.order.exception.OrderNotCancellableException;
import com.backend.domain.user.entity.User;
import com.backend.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user_status", columnList = "user_id, status")
})
@SQLRestriction("deleted_at IS NULL")
public class Order extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @org.hibernate.annotations.BatchSize(size = 100)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 100)
    private String detailAddress;

    @Column(columnDefinition = "TEXT")
    private String deliveryNote;

    @Builder
    public Order(String orderNumber, User user, BigDecimal totalAmount, BigDecimal shippingFee,
            String recipientName, String phoneNumber, String zipCode,
            String address, String detailAddress, String deliveryNote) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.items = new ArrayList<>();
        this.totalAmount = totalAmount;
        this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        this.grandTotal = totalAmount.add(this.shippingFee);
        this.status = OrderStatus.PENDING;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.deliveryNote = deliveryNote;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException();
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStatusException();
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void cancel(String reason) {
        if (!isCancellable()) {
            throw new OrderNotCancellableException();
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
    }

    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }

    public boolean isCancellable() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.CONFIRMED || this.status == OrderStatus.SHIPPED;
    }

    public void anonymize() {
        this.recipientName = "탈퇴회원";
        this.phoneNumber = "00000000000";
        this.address = "삭제됨";
        this.detailAddress = null;
        this.zipCode = "00000";
        this.deliveryNote = null;
    }
}
