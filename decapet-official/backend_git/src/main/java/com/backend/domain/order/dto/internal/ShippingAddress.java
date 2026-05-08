package com.backend.domain.order.dto.internal;

public record ShippingAddress(
        String recipientName,
        String phoneNumber,
        String zipCode,
        String address,
        String detailAddress,
        String deliveryNote
) {
}
