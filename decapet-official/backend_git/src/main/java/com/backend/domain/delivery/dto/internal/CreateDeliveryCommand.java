package com.backend.domain.delivery.dto.internal;

import com.backend.domain.order.dto.internal.ShippingAddress;
import com.backend.domain.order.entity.Order;
import com.backend.domain.user.entity.User;

public record CreateDeliveryCommand(
        Order order,
        User user,
        ShippingAddress shippingAddress
) {
}
