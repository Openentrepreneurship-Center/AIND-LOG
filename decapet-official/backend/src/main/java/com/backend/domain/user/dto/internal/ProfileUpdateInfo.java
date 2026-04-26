package com.backend.domain.user.dto.internal;

public record ProfileUpdateInfo(
    String name,
    String phone,
    String zipCode,
    String address,
    String detailAddress,
    String recipientName,
    String recipientPhone
) {
}
