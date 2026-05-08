package com.backend.domain.delivery.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;

import com.backend.domain.delivery.exception.DeliveryNotShippingException;
import com.backend.domain.order.entity.Order;
import com.backend.domain.user.entity.User;
import com.backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "deliveries", indexes = {
    @Index(name = "idx_deliveries_user_id", columnList = "user_id"),
    @Index(name = "idx_deliveries_status", columnList = "status")
})
@SQLRestriction("deleted_at IS NULL")
public class Delivery extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryStatus status;

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

    private LocalDateTime deliveredAt;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Builder
    public Delivery(Order order, User user, String recipientName, String phoneNumber,
            String zipCode, String address, String detailAddress, String deliveryNote) {
        this.order = order;
        this.user = user;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.deliveryNote = deliveryNote;
        this.status = DeliveryStatus.SHIPPING;
    }

    public void assignTracking(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void cancel(String reason) {
        if (this.status != DeliveryStatus.SHIPPING) {
            throw new DeliveryNotShippingException();
        }
        this.status = DeliveryStatus.CANCELLED;
        this.cancelReason = reason;
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
