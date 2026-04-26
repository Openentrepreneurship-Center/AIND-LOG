package com.backend.domain.order.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.order.dto.request.CreateOrderRequest;
import com.backend.domain.order.dto.response.OrderListResponse;
import com.backend.domain.order.dto.response.OrderResponse;
import com.backend.domain.order.entity.OrderStatus;
import com.backend.domain.order.service.OrderService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse> createOrderFromCart(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid CreateOrderRequest request) {
        OrderResponse response = orderService.createOrderFromCart(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ORDER_CREATE_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getOrders(
            @AuthenticationPrincipal String userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OrderListResponse response = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ORDER_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/status/{status}")
    public ResponseEntity<SuccessResponse> getOrdersByStatus(
            @AuthenticationPrincipal String userId,
            @PathVariable OrderStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OrderListResponse response = orderService.getUserOrdersByStatus(userId, status, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ORDER_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{orderId}")
    public ResponseEntity<SuccessResponse> getOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable String orderId) {
        OrderResponse response = orderService.getOrder(userId, orderId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ORDER_GET_SUCCESS, response));
    }

    @Override
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<SuccessResponse> cancelOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable String orderId,
            @RequestParam(required = false) String reason) {
        OrderResponse response = orderService.cancelOrder(userId, orderId, reason);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ORDER_CANCEL_SUCCESS, response));
    }
}
