package com.backend.domain.order.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.order.dto.internal.CreateOrderCommand;
import com.backend.domain.order.dto.internal.ShippingAddress;
import com.backend.domain.order.entity.Order;
import com.backend.domain.order.entity.OrderItem;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderCommand command) {
        ShippingAddress address = command.shippingAddress();

        Order order = Order.builder()
                .orderNumber(command.orderNumber())
                .user(command.user())
                .totalAmount(command.totalAmount())
                .shippingFee(command.shippingFee())
                .recipientName(address.recipientName())
                .phoneNumber(address.phoneNumber())
                .zipCode(address.zipCode())
                .address(address.address())
                .detailAddress(address.detailAddress())
                .deliveryNote(address.deliveryNote())
                .build();

        for (OrderItem item : command.items()) {
            order.addItem(item);
        }

        return order;
    }
}
