package com.backend.domain.order.controller;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.backend.domain.order.entity.OrderStatus;
import com.backend.global.common.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 - 주문", description = "관리자 주문 관리")
@SecurityRequirement(name = "bearerAuth")
public interface AdminOrderApi {

    @Operation(summary = "주문 목록 조회", description = "전체 주문 목록을 조회합니다. 상태별/날짜/검색어 필터 가능.")
    @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공")
    ResponseEntity<SuccessResponse> getAllOrders(
            @Parameter(description = "주문 상태 필터") OrderStatus status,
            @Parameter(description = "삭제된 주문 포함 여부") boolean includeDeleted,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") LocalDate startDate,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") LocalDate endDate,
            @Parameter(description = "검색어") String searchText,
            @Parameter(description = "검색 카테고리 (orderNumber, recipientName, phone)") String searchCategory,
            @ParameterObject Pageable pageable);

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공")
    ResponseEntity<SuccessResponse> getOrder(
            @Parameter(description = "주문 ID") String orderId);

    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @ApiResponse(responseCode = "200", description = "주문 취소 성공")
    ResponseEntity<SuccessResponse> cancelOrder(
            @Parameter(description = "주문 ID") String orderId,
            @Parameter(description = "취소 사유") String reason);

    @Operation(summary = "주문 삭제", description = "취소된 주문을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "주문 삭제 성공")
    ResponseEntity<SuccessResponse> deleteOrder(
            @Parameter(description = "주문 ID") String orderId);
}
