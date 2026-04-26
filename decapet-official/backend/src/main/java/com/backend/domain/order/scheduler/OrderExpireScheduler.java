package com.backend.domain.order.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.domain.order.entity.Order;
import com.backend.domain.order.entity.OrderStatus;
import com.backend.domain.order.repository.OrderRepository;
import com.backend.domain.order.service.OrderService;
import com.backend.domain.payment.entity.PaymentStatus;
import com.backend.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.backend.global.util.DateTimeUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpireScheduler {

    private static final int ORDER_EXPIRE_MINUTES = 30;

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void cancelExpiredOrders() {
        try {
            LocalDateTime expiredTime = DateTimeUtil.now().minusMinutes(ORDER_EXPIRE_MINUTES);

            List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(
                    OrderStatus.PENDING, expiredTime);

            if (expiredOrders.isEmpty()) {
                return;
            }

            int deleted = 0;
            for (Order order : expiredOrders) {
                try {
                    paymentRepository.findByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING)
                            .filter(p -> p.getPgTransactionId() != null)
                            .ifPresent(p -> orderService.restoreProductStock(order));
                    orderService.softDeleteOrder(order);
                    deleted++;
                } catch (Exception e) {
                    log.warn("만료 주문 삭제 실패: orderId={}, reason={}", order.getId(), e.getMessage());
                }
            }

            log.info("만료 주문 삭제 완료: {}건 처리 (전체 {}건)", deleted, expiredOrders.size());
        } catch (Exception e) {
            log.error("만료 주문 취소 스케줄러 실패", e);
        }
    }
}
