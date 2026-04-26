package com.backend.domain.order.dto.internal;

import java.math.BigDecimal;
import java.util.List;

import com.backend.domain.order.entity.OrderItem;
import com.backend.domain.user.entity.User;

public record CreateOrderCommand(
        String orderNumber,
        User user,
        List<OrderItem> items,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        ShippingAddress shippingAddress
) {
}
