package com.backend.domain.order.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.order.dto.internal.ShippingAddress;
import com.backend.domain.order.dto.request.ShippingAddressRequest;

@Component
public class ShippingAddressMapper {

    public ShippingAddress toInternal(ShippingAddressRequest request) {
        return new ShippingAddress(
                request.recipientName(),
                request.phoneNumber(),
                request.zipCode(),
                request.address(),
                request.detailAddress(),
                request.deliveryNote()
        );
    }
}
