package com.backend.domain.delivery.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.delivery.dto.response.DeliveryResponse;
import com.backend.domain.delivery.entity.Delivery;

@Component
public class DeliveryResponseMapper {

    public DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrder().getId(),
                delivery.getTrackingNumber(),
                delivery.getStatus(),
                delivery.getRecipientName(),
                delivery.getPhoneNumber(),
                delivery.getZipCode(),
                delivery.getAddress(),
                delivery.getDetailAddress(),
                delivery.getDeliveryNote(),
                delivery.getDeliveredAt(),
                delivery.getCancelReason(),
                delivery.getCreatedAt()
        );
    }
}
