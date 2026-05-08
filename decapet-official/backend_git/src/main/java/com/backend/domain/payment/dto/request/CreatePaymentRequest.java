package com.backend.domain.payment.dto.request;

import java.util.List;

import com.backend.domain.payment.entity.PaymentMethod;
import com.backend.domain.payment.entity.PaymentType;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotNull(message = "결제 유형을 선택해주세요.")
        PaymentType paymentType,

        List<String> appointmentIds,

        String orderId,

        String prescriptionId,

        @NotNull(message = "결제 수단을 선택해주세요.")
        PaymentMethod paymentMethod,

        String accountHolder,

        String cashReceiptType,

        String cashReceiptNumber
) {
}
