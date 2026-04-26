package com.backend.domain.order.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.cart.entity.CartItem;
import com.backend.domain.order.entity.OrderItem;

@Component
public class OrderItemMapper {

    public OrderItem toEntity(CartItem cartItem) {
        return OrderItem.fromCartItem(cartItem);
    }
}
