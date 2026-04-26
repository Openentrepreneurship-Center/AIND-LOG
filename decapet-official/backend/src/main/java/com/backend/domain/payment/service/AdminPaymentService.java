package com.backend.domain.payment.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.order.entity.Order;
import com.backend.domain.order.service.OrderService;
import com.backend.domain.payment.dto.mapper.PaymentResponseMapper;
import com.backend.domain.payment.dto.response.PaymentResponse;
import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.exception.PaymentNotPendingException;
import com.backend.domain.payment.repository.PaymentRepository;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.global.aop.AdminAudit;
import com.backend.global.common.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentResponseMapper paymentResponseMapper;
    private final OrderService orderService;

    public PageResponse<PaymentResponse> getAllPayments(Pageable pageable) {
        return PageResponse.from(
                paymentRepository.findAll(pageable)
                        .map(paymentResponseMapper::toResponse)
        );
    }

    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.getById(paymentId);
        return paymentResponseMapper.toResponse(payment);
    }

    @AdminAudit(action = "PAYMENT_CONFIRM_DEPOSIT", targetType = "Payment")
    @Transactional
    public PaymentResponse confirmDeposit(String paymentId) {
        Payment payment = paymentRepository.getByIdForUpdate(paymentId);

        if (!payment.isPending()) {
            throw new PaymentNotPendingException();
        }

        payment.complete(null);

        handlePaymentCompletion(payment);

        return paymentResponseMapper.toResponse(payment);
    }

    private void handlePaymentCompletion(Payment payment) {
        if (payment.isAppointmentPayment()) {
            for (Appointment appointment : payment.getAppointments()) {
                appointment.complete();
            }
        } else if (payment.isPrescriptionPayment()) {
            // 처방전은 APPROVED 상태 유지 (PAID 상태 제거됨)
        } else {
            Order order = payment.getOrder();
            orderService.completeOrderPayment(order);
        }
    }
}
