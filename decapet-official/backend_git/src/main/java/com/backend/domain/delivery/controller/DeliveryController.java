package com.backend.domain.delivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.delivery.dto.response.DeliveryResponse;
import com.backend.domain.delivery.service.DeliveryService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    @Override
    @GetMapping("/order/{orderId}")
    public ResponseEntity<SuccessResponse> getDeliveryByOrderId(
            @AuthenticationPrincipal String userId,
            @PathVariable String orderId) {
        DeliveryResponse response = deliveryService.getDeliveryByOrderId(userId, orderId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.DELIVERY_GET_SUCCESS, response));
    }
}
