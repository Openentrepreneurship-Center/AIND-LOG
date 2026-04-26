package com.backend.domain.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.delivery.dto.internal.AssignTrackingCommand;
import com.backend.domain.delivery.dto.internal.CreateDeliveryCommand;
import com.backend.domain.delivery.dto.mapper.DeliveryMapper;
import com.backend.domain.delivery.dto.mapper.DeliveryResponseMapper;
import com.backend.domain.delivery.dto.response.DeliveryResponse;
import com.backend.domain.delivery.entity.Delivery;
import com.backend.domain.delivery.exception.DeliveryNotFoundException;
import com.backend.domain.delivery.exception.DuplicateTrackingNumberException;
import com.backend.domain.delivery.repository.DeliveryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryResponseMapper deliveryResponseMapper;

    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(String userId, String orderId) {
        Delivery delivery = deliveryRepository.getByOrderIdOrThrow(orderId);
        if (delivery.getUser() == null || !delivery.getUser().getId().equals(userId)) {
            throw new DeliveryNotFoundException();
        }
        return deliveryResponseMapper.toResponse(delivery);
    }

    @Transactional
    public void createDelivery(CreateDeliveryCommand command) {
        Delivery delivery = deliveryMapper.toEntity(command);
		deliveryRepository.save(delivery);
	}

    @Transactional
    public DeliveryResponse assignTracking(AssignTrackingCommand command) {
        if (deliveryRepository.existsByTrackingNumber(command.trackingNumber())) {
            throw new DuplicateTrackingNumberException(command.trackingNumber());
        }

        Delivery delivery = deliveryRepository.getByIdOrThrow(command.deliveryId());
        delivery.assignTracking(command.trackingNumber());
        return deliveryResponseMapper.toResponse(delivery);
    }
}
