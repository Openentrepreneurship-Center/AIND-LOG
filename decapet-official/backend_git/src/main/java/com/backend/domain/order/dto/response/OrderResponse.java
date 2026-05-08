package com.backend.domain.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.backend.domain.order.entity.OrderStatus;

public record OrderResponse(
        String id,
        String orderNumber,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        BigDecimal grandTotal,
        OrderStatus status,
        ShippingAddressResponse shippingAddress,
        boolean cancellable,
        String cancelReason,
        String trackingNumber,
        LocalDateTime createdAt
) {
}
