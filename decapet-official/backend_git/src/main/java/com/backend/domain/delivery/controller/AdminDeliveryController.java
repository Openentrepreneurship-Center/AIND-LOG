package com.backend.domain.delivery.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.delivery.dto.request.AssignTrackingRequest;
import com.backend.domain.delivery.dto.request.BulkAssignTrackingRequest;
import com.backend.domain.delivery.dto.request.CancelDeliveryRequest;
import com.backend.domain.delivery.dto.response.BulkTrackingResponse;
import com.backend.domain.delivery.dto.response.DeliveryResponse;
import com.backend.domain.delivery.entity.DeliveryStatus;
import com.backend.domain.delivery.service.AdminDeliveryService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/deliveries")
@RequiredArgsConstructor
public class AdminDeliveryController implements AdminDeliveryApi {

    private final AdminDeliveryService adminDeliveryService;

    @GetMapping
    public ResponseEntity<SuccessResponse> getDeliveriesByStatus(
            @RequestParam DeliveryStatus status,
            Pageable pageable) {
        PageResponse<DeliveryResponse> response = adminDeliveryService.getDeliveriesByStatus(status, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.DELIVERY_LIST_SUCCESS, response));
    }

    @PostMapping("/tracking/bulk")
    public ResponseEntity<SuccessResponse> bulkAssignTracking(
            @RequestBody @Valid BulkAssignTrackingRequest request) {
        BulkTrackingResponse response = adminDeliveryService.bulkAssignTracking(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELIVERY_UPDATE_SUCCESS, response));
    }

    @PostMapping("/{deliveryId}/tracking")
    public ResponseEntity<SuccessResponse> assignTracking(
            @PathVariable String deliveryId,
            @RequestBody @Valid AssignTrackingRequest request) {
        DeliveryResponse response = adminDeliveryService.assignTracking(deliveryId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELIVERY_UPDATE_SUCCESS, response));
    }

    @PostMapping("/{deliveryId}/cancel")
    public ResponseEntity<SuccessResponse> cancelDelivery(
            @PathVariable String deliveryId,
            @RequestBody @Valid CancelDeliveryRequest request) {
        DeliveryResponse response = adminDeliveryService.cancelDelivery(deliveryId, request.reason());
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELIVERY_UPDATE_SUCCESS, response));
    }
}
