package com.backend.domain.delivery.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.delivery.entity.DeliveryStatus;

public record DeliveryResponse(
        String id,
        String orderId,
        String trackingNumber,
        DeliveryStatus status,
        String recipientName,
        String phoneNumber,
        String zipCode,
        String address,
        String detailAddress,
        String deliveryNote,
        LocalDateTime deliveredAt,
        String cancelReason,
        LocalDateTime createdAt
) {
}
