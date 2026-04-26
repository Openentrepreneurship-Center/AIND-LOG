package com.backend.domain.payment.dto.response;

public record TossPaymentsConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        String method,
        int totalAmount,
        String approvedAt
) {
}
