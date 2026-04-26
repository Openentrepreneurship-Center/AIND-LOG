package com.backend.domain.order.dto.response;

public record ShippingAddressResponse(
        String recipientName,
        String phoneNumber,
        String zipCode,
        String address,
        String detailAddress,
        String deliveryNote
) {
}
