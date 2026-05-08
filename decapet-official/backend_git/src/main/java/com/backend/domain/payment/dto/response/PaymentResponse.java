package com.backend.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.backend.domain.payment.entity.PaymentMethod;
import com.backend.domain.payment.entity.PaymentStatus;
import com.backend.domain.payment.entity.PaymentType;

public record PaymentResponse(
        String id,
        PaymentType paymentType,
        String referenceId,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String pgTransactionId,
        String accountHolder,
        String cashReceiptType,
        String cashReceiptNumber,
        LocalDateTime createdAt
) {
}
