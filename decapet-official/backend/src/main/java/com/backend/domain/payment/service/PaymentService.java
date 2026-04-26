package com.backend.domain.payment.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.global.common.PageResponse;

import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.appointment.entity.AppointmentStatus;
import com.backend.domain.appointment.repository.AppointmentRepository;
import com.backend.domain.delivery.dto.internal.CreateDeliveryCommand;
import com.backend.domain.delivery.service.DeliveryService;
import com.backend.domain.order.dto.internal.ShippingAddress;
import com.backend.domain.order.entity.Order;
import com.backend.domain.order.exception.OrderNotPendingException;
import com.backend.domain.order.service.OrderService;
import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;
import com.backend.domain.payment.dto.mapper.PaymentMapper;
import com.backend.domain.payment.dto.mapper.PaymentResponseMapper;
import com.backend.domain.payment.dto.request.CompletePaymentRequest;
import com.backend.domain.payment.dto.request.CreatePaymentRequest;
import com.backend.domain.payment.dto.response.PaymentResponse;
import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.entity.PaymentStatus;
import com.backend.domain.payment.entity.PaymentType;
import com.backend.domain.payment.exception.AppointmentNotApprovedException;
import com.backend.domain.payment.exception.PaymentAlreadyExistsException;
import com.backend.domain.payment.exception.PaymentAmountMismatchException;
import com.backend.domain.payment.exception.PaymentConfirmFailedException;
import com.backend.domain.payment.exception.PaymentNotFoundException;
import com.backend.domain.payment.exception.PaymentNotPendingException;
import com.backend.domain.payment.exception.PrescriptionNotApprovedException;
import com.backend.domain.payment.repository.PaymentRepository;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.prescription.entity.PrescriptionStatus;
import com.backend.domain.prescription.repository.PrescriptionRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final OrderService orderService;
    private final DeliveryService deliveryService;
    private final TossPaymentsService tossPaymentsService;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentResponseMapper paymentResponseMapper;
    private final TransactionTemplate transactionTemplate;

    @Transactional
    public PaymentResponse createPayment(String userId, CreatePaymentRequest request) {
        User user = userRepository.getReferenceById(userId);
        Payment payment;

        if (request.paymentType() == PaymentType.APPOINTMENT) {
            payment = createAppointmentPayment(user, request);
        } else if (request.paymentType() == PaymentType.PRESCRIPTION) {
            payment = createPrescriptionPayment(user, request);
        } else {
            payment = createOrderPayment(user, request);
        }

        Payment saved = paymentRepository.save(payment);
        return paymentResponseMapper.toResponse(saved);
    }

    private void validateAccountHolder(CreatePaymentRequest request) {
        if (request.accountHolder() == null || request.accountHolder().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private Payment createAppointmentPayment(User user, CreatePaymentRequest request) {
        validateAccountHolder(request);

        if (request.appointmentIds() == null || request.appointmentIds().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        List<Appointment> appointments = new ArrayList<>();
        for (String appointmentId : request.appointmentIds()) {
            Appointment appointment = appointmentRepository.getById(appointmentId);

            if (appointment.getUser() == null || !appointment.getUser().getId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }

            if (appointment.getStatus() != AppointmentStatus.PENDING
                    && appointment.getStatus() != AppointmentStatus.APPROVED) {
                throw new AppointmentNotApprovedException();
            }

            if (paymentRepository.existsByAppointmentIds(appointmentId)) {
                throw new PaymentAlreadyExistsException();
            }

            appointments.add(appointment);
        }

        return paymentMapper.toEntityFromAppointments(user, appointments, request.paymentMethod(),
                request.accountHolder(),
                request.cashReceiptType(), request.cashReceiptNumber());
    }

    private Payment createOrderPayment(User user, CreatePaymentRequest request) {
        Order order = orderService.getOrderEntity(request.orderId());

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!order.isPending()) {
            throw new OrderNotPendingException();
        }

        if (paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.COMPLETED)) {
            throw new PaymentAlreadyExistsException();
        }

        if (paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING)) {
            throw new PaymentAlreadyExistsException();
        }

        return paymentMapper.toEntityFromOrder(user, order, request.paymentMethod());
    }

    private Payment createPrescriptionPayment(User user, CreatePaymentRequest request) {
        validateAccountHolder(request);
        Prescription prescription = prescriptionRepository.findByIdOrThrow(request.prescriptionId());

        if (!prescription.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (prescription.getStatus() != PrescriptionStatus.APPROVED) {
            throw new PrescriptionNotApprovedException();
        }

        if (paymentRepository.existsByPrescriptionIdAndStatusNot(prescription.getId(), PaymentStatus.CANCELLED)) {
            throw new PaymentAlreadyExistsException();
        }

        return paymentMapper.toEntityFromPrescription(user, prescription, request.paymentMethod(),
                request.accountHolder(),
                request.cashReceiptType(), request.cashReceiptNumber());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentResponse completePayment(String userId, String paymentId, CompletePaymentRequest request) {
        // 1단계: 트랜잭션으로 결제 조회 + 검증
        Payment payment = transactionTemplate.execute(status -> {
            Payment p = paymentRepository.getByIdForUpdate(paymentId);

            if (p.getUser() == null || !p.getUser().getId().equals(userId)) {
                throw new PaymentNotFoundException();
            }

            if (!p.isPending()) {
                throw new PaymentNotPendingException();
            }

            if (p.isOrderPayment() && p.getAmount().compareTo(request.amount()) != 0) {
                p.fail("결제 금액 불일치");
                throw new PaymentAmountMismatchException();
            }

            return p;
        });

        // 2단계: 주문 결제인 경우 재고 차감 + paymentKey 저장 + PG 승인
        if (payment.isOrderPayment()) {
            // 2-1: 재고 차감
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    Order order = orderService.getOrderEntity(payment.getOrder().getId());
                    orderService.validateAndDecreaseStock(order);
                });
            } catch (Exception e) {
                transactionTemplate.executeWithoutResult(status -> {
                    paymentRepository.getByIdForUpdate(paymentId).fail("재고 부족");
                    Order order = orderService.getOrderEntity(payment.getOrder().getId());
                    orderService.softDeleteOrder(order);
                });
                throw e;
            }

            // 2-2: paymentKey를 미리 저장 (크래시 복구용 — 웹훅에서 조회 가능)
            transactionTemplate.executeWithoutResult(status -> {
                paymentRepository.getByIdForUpdate(paymentId).savePgPaymentKey(request.paymentKey());
            });

            // 2-3: 트랜잭션 밖에서 토스페이먼츠 승인
            try {
                tossPaymentsService.confirmPayment(
                        request.paymentKey(), request.orderId(), request.amount());
            } catch (PaymentConfirmFailedException e) {
                transactionTemplate.executeWithoutResult(status -> {
                    Order order = orderService.getOrderEntity(payment.getOrder().getId());
                    orderService.restoreProductStock(order);
                    paymentRepository.getByIdForUpdate(paymentId).fail(e.getMessage());
                    orderService.softDeleteOrder(order);
                });
                throw e;
            }
        }

        // 4단계: 트랜잭션으로 결제 완료 처리
        try {
            return transactionTemplate.execute(status -> {
                Payment p = paymentRepository.getByIdForUpdate(paymentId);
                p.complete(request.paymentKey());
                handlePaymentCompletion(p);
                return paymentResponseMapper.toResponse(p);
            });
        } catch (Exception e) {
            // PG 승인 성공 + DB 커밋 실패 시 보상: PG 취소 + 재고 복원
            if (payment.isOrderPayment()) {
                try {
                    tossPaymentsService.cancelPayment(request.paymentKey(), "DB 처리 실패로 인한 자동 취소");
                } catch (Exception cancelEx) {
                    log.error("보상 취소 실패 — 수동 처리 필요: paymentKey={}, 원인={}",
                            request.paymentKey(), cancelEx.getMessage());
                }
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        Order order = orderService.getOrderEntity(payment.getOrder().getId());
                        orderService.restoreProductStock(order);
                        orderService.softDeleteOrder(order);
                    });
                } catch (Exception restoreEx) {
                    log.error("재고 복원 실패 — 수동 처리 필요: orderId={}, 원인={}",
                            payment.getOrder().getId(), restoreEx.getMessage());
                }
            }
            throw e;
        }
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
            createDelivery(order);
        }
    }

    private void createDelivery(Order order) {
        ShippingAddress shippingAddress = new ShippingAddress(
                order.getRecipientName(),
                order.getPhoneNumber(),
                order.getZipCode(),
                order.getAddress(),
                order.getDetailAddress(),
                order.getDeliveryNote()
        );

        CreateDeliveryCommand deliveryCommand = new CreateDeliveryCommand(
                order,
                order.getUser(),
                shippingAddress
        );
        deliveryService.createDelivery(deliveryCommand);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void handleWebhookPaymentStatusChanged(String paymentKey, String status) {
        if (!"DONE".equals(status)) {
            log.info("웹훅: DONE이 아닌 상태 무시 — paymentKey={}, status={}", paymentKey, status);
            return;
        }

        Payment payment = paymentRepository.findByPgTransactionId(paymentKey).orElse(null);
        if (payment == null) {
            log.warn("웹훅: 결제 정보를 찾을 수 없음 — paymentKey={}", paymentKey);
            return;
        }

        if (payment.isCompleted()) {
            log.info("웹훅: 이미 완료된 결제 — paymentKey={}", paymentKey);
            return;
        }

        if (!payment.isPending()) {
            log.warn("웹훅: PENDING이 아닌 결제 — paymentKey={}, status={}", paymentKey, payment.getStatus());
            return;
        }

        // PG 승인은 완료되었지만 DB 반영이 안 된 결제 → 복구 진행
        log.warn("웹훅: 미완료 결제 복구 시작 — paymentKey={}, paymentId={}", paymentKey, payment.getId());

        try {
            transactionTemplate.executeWithoutResult(txStatus -> {
                Payment p = paymentRepository.getByIdForUpdate(payment.getId());
                if (!p.isPending()) {
                    return;
                }
                p.complete(paymentKey);
                handlePaymentCompletion(p);
            });
            log.info("웹훅: 결제 복구 완료 — paymentKey={}", paymentKey);
        } catch (Exception e) {
            log.error("웹훅: 결제 복구 실패 — 수동 처리 필요: paymentKey={}, 원인={}", paymentKey, e.getMessage());
        }
    }

    @Transactional
    public void cancelPayment(String userId, String paymentId) {
        Payment payment = paymentRepository.getById(paymentId);

        if (payment.getUser() == null || !payment.getUser().getId().equals(userId)) {
            throw new PaymentNotFoundException();
        }

        if (!payment.isPending()) {
            throw new PaymentNotPendingException();
        }

        payment.cancel();

        if (payment.isOrderPayment()) {
            orderService.softDeleteOrder(payment.getOrder());
        }
    }

    public PageResponse<PaymentResponse> getMyPayments(String userId, Pageable pageable) {
        return PageResponse.from(
            paymentRepository.findByUserId(userId, pageable)
                .map(paymentResponseMapper::toResponse)
        );
    }

    public PaymentResponse getPayment(String userId, String paymentId) {
        Payment payment = paymentRepository.getById(paymentId);
        if (payment.getUser() == null || !payment.getUser().getId().equals(userId)) {
            throw new PaymentNotFoundException();
        }
        return paymentResponseMapper.toResponse(payment);
    }
}
