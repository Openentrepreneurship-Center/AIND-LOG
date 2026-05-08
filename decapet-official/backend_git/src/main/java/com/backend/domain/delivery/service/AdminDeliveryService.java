package com.backend.domain.delivery.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.global.common.PageResponse;

import com.backend.domain.delivery.dto.mapper.DeliveryResponseMapper;
import com.backend.domain.delivery.dto.request.AssignTrackingRequest;
import com.backend.domain.delivery.dto.request.BulkAssignTrackingRequest;
import com.backend.domain.delivery.dto.response.BulkTrackingResponse;
import com.backend.domain.delivery.dto.response.DeliveryResponse;
import com.backend.domain.delivery.entity.Delivery;
import com.backend.domain.delivery.entity.DeliveryStatus;
import com.backend.domain.delivery.repository.DeliveryRepository;
import com.backend.domain.order.entity.Order;
import com.backend.domain.order.entity.OrderStatus;
import com.backend.domain.order.repository.OrderRepository;
import com.backend.domain.order.service.OrderService;
import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.entity.PaymentStatus;
import com.backend.domain.payment.repository.PaymentRepository;
import com.backend.domain.payment.service.TossPaymentsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;
    private final DeliveryResponseMapper deliveryResponseMapper;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentsService tossPaymentsService;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public PageResponse<DeliveryResponse> getDeliveriesByStatus(DeliveryStatus status, Pageable pageable) {
        return PageResponse.from(
            deliveryRepository.findByStatus(status, pageable)
                .map(deliveryResponseMapper::toResponse)
        );
    }

    @Transactional
    public DeliveryResponse assignTracking(String deliveryId, AssignTrackingRequest request) {
        Delivery delivery = deliveryRepository.getByIdOrThrow(deliveryId);
        delivery.assignTracking(request.trackingNumber());

        Order order = delivery.getOrder();
        if (order != null && order.getStatus() == OrderStatus.CONFIRMED) {
            order.ship();
        }

        return deliveryResponseMapper.toResponse(delivery);
    }

    public BulkTrackingResponse bulkAssignTracking(BulkAssignTrackingRequest request) {
        int successCount = 0;
        List<BulkTrackingResponse.FailureDetail> failures = new ArrayList<>();

        for (BulkAssignTrackingRequest.Item item : request.items()) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    Order order = orderRepository.findByOrderNumber(item.orderNumber())
                            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

                    Delivery delivery = deliveryRepository.findByOrder_Id(order.getId())
                            .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

                    delivery.assignTracking(item.trackingNumber());
                    if (order.getStatus() == OrderStatus.CONFIRMED) {
                        order.ship();
                    }
                });
                successCount++;
            } catch (Exception e) {
                String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                failures.add(new BulkTrackingResponse.FailureDetail(item.orderNumber(), msg));
            }
        }

        return new BulkTrackingResponse(successCount, failures.size(), failures);
    }

    @Transactional
    public DeliveryResponse cancelDelivery(String deliveryId, String reason) {
        Delivery delivery = deliveryRepository.getByIdOrThrow(deliveryId);
        delivery.cancel(reason);

        // 주문 상태 변경 + 재고 복구
        Order order = delivery.getOrder();
        if (order != null && order.isCancellable()) {
            orderService.cancelOrderByDelivery(order);

            // 결제 환불 처리
            paymentRepository.findByOrderIdAndStatus(order.getId(), PaymentStatus.COMPLETED)
                    .ifPresent(payment -> {
                        if (payment.getPgTransactionId() != null) {
                            try {
                                tossPaymentsService.cancelPayment(payment.getPgTransactionId(), reason);
                            } catch (Exception e) {
                                log.error("배송 취소 환불 실패 — 수동 처리 필요: paymentId={}, reason={}",
                                        payment.getId(), e.getMessage());
                            }
                        }
                        payment.cancel();
                    });
        }

        return deliveryResponseMapper.toResponse(delivery);
    }
}
