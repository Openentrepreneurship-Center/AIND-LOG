package com.backend.domain.delivery.dto.internal;

public record AssignTrackingCommand(
        String deliveryId,
        String trackingNumber
) {
}
