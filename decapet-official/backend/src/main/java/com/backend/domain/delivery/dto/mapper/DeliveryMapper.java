package com.backend.domain.delivery.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.delivery.dto.internal.CreateDeliveryCommand;
import com.backend.domain.delivery.entity.Delivery;
import com.backend.domain.order.dto.internal.ShippingAddress;

@Component
public class DeliveryMapper {

    public Delivery toEntity(CreateDeliveryCommand command) {
        ShippingAddress address = command.shippingAddress();

        return Delivery.builder()
                .order(command.order())
                .user(command.user())
                .recipientName(address.recipientName())
                .phoneNumber(address.phoneNumber())
                .zipCode(address.zipCode())
                .address(address.address())
                .detailAddress(address.detailAddress())
                .deliveryNote(address.deliveryNote())
                .build();
    }
}
