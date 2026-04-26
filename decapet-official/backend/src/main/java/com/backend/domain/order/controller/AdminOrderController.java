package com.backend.domain.order.controller;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.order.entity.OrderStatus;
import com.backend.domain.order.service.AdminOrderService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController implements AdminOrderApi {

	private final AdminOrderService adminOrderService;

	@GetMapping
	public ResponseEntity<SuccessResponse> getAllOrders(
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) String searchText,
			@RequestParam(required = false) String searchCategory,
			@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(
				SuccessResponse.of(SuccessCode.ADMIN_ORDER_LIST_SUCCESS,
						adminOrderService.getAllOrders(status, includeDeleted, startDate, endDate, searchText, searchCategory, pageable)));
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<SuccessResponse> getOrder(
			@PathVariable String orderId) {
		return ResponseEntity.ok(
				SuccessResponse.of(SuccessCode.ADMIN_ORDER_DETAIL_SUCCESS,
						adminOrderService.getOrder(orderId)));
	}

	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<SuccessResponse> cancelOrder(
			@PathVariable String orderId,
			@RequestParam(required = false) String reason) {
		adminOrderService.cancelOrder(orderId, reason);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS));
	}

	@DeleteMapping("/{orderId}")
	public ResponseEntity<SuccessResponse> deleteOrder(
			@PathVariable String orderId) {
		adminOrderService.deleteOrder(orderId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELETE_SUCCESS));
	}
}
