package com.backend.domain.payment.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.payment.dto.response.PaymentResponse;
import com.backend.domain.payment.entity.Payment;

@Component
public class PaymentResponseMapper {

    public PaymentResponse toResponse(Payment payment) {
        String referenceId;
        if (payment.isAppointmentPayment()) {
            referenceId = payment.getFirstAppointment() != null ? payment.getFirstAppointment().getId() : null;
        } else if (payment.isPrescriptionPayment()) {
            referenceId = payment.getPrescription().getId();
        } else {
            referenceId = payment.getOrder().getId();
        }

        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentType(),
                referenceId,
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getPgTransactionId(),
                payment.getAccountHolder(),
                payment.getCashReceiptType(),
                payment.getCashReceiptNumber(),
                payment.getCreatedAt()
        );
    }
}
