package com.backend.domain.delivery.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.backend.domain.delivery.dto.request.AssignTrackingRequest;
import com.backend.domain.delivery.dto.request.BulkAssignTrackingRequest;
import com.backend.domain.delivery.dto.request.CancelDeliveryRequest;
import com.backend.domain.delivery.entity.DeliveryStatus;
import com.backend.global.common.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[Admin] 배송", description = "배송 관리")
public interface AdminDeliveryApi {

    @Operation(
        summary = "배송 목록 조회",
        description = """
            상태별 배송 목록을 조회합니다.

            **조회 기능**
            - 배송 상태별 필터링
            - 페이징 지원
            - 최신 생성 순으로 정렬

            **배송 상태**
            - SHIPPING: 배송 중
            - CANCELLED: 배송 취소

            **응답 정보**
            - 배송 ID, 주문 ID
            - 운송장 번호
            - 배송 상태, 수령인 정보, 배송지 주소

            **사용 화면**: 관리자 > 배송 관리 > 목록
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "배송 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "D002",
                          "httpStatus": "OK",
                          "message": "배송 목록 조회 성공",
                          "data": {
                            "content": [
                              {
                                "id": "delivery123",
                                "orderId": "order123",
                                "trackingNumber": "1234567890123",

                                "status": "SHIPPING",
                                "recipientName": "홍길동",
                                "phoneNumber": "01012345678",
                                "address": "서울시 강남구",
                                "createdAt": "2025-01-16T10:00:00"
                              }
                            ],
                            "pageable": {
                              "pageNumber": 0,
                              "pageSize": 10
                            },
                            "totalElements": 1,
                            "totalPages": 1
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "A001",
                          "message": "인증이 필요합니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (관리자만 접근 가능)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "A002",
                          "message": "접근 권한이 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> getDeliveriesByStatus(
        @Parameter(description = "배송 상태 (SHIPPING, CANCELLED)", required = true, example = "SHIPPING")
        DeliveryStatus status,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "운송장 일괄 등록",
        description = """
            엑셀 업로드를 통해 여러 주문의 운송장 정보를 일괄 등록합니다.

            **처리 흐름**
            1. 주문번호로 주문 조회
            2. 주문에 연결된 배송 조회
            3. 운송장 번호 등록
            4. 실패 건은 건너뛰고 결과에 포함

            **사용 화면**: 관리자 > 주문·배송 관리 > 운송장 업로드
            """
    )
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> bulkAssignTracking(
        @Valid BulkAssignTrackingRequest request
    );

    @Operation(
        summary = "운송장 등록",
        description = """
            배송에 운송장 정보를 등록합니다.

            **처리 흐름**
            1. 운송장 번호 등록
            2. 고객에게 배송 시작 알림
            3. 운송장 번호로 배송 추적 가능

            **제약사항**
            - SHIPPING 상태의 배송만 등록 가능
            - 운송장 번호는 중복 등록 불가
            - 운송장 번호 필수

            **사용 화면**: 관리자 > 배송 관리 > 운송장 등록
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "운송장 등록 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "AD050",
                          "httpStatus": "CREATED",
                          "message": "배송 생성 성공",
                          "data": {
                            "id": "delivery123",
                            "orderId": "order123",
                            "trackingNumber": "1234567890123",
                            "carrier": "CJ대한통운",
                            "status": "SHIPPING",
                            "recipientName": "홍길동",
                            "phoneNumber": "01012345678",
                            "zipCode": "12345",
                            "address": "서울시 강남구 테헤란로 123",
                            "deliveryNote": "문 앞에 놓아주세요",
                            "deliveredAt": null,
                            "cancelReason": null,
                            "createdAt": "2025-01-16T10:00:00"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "유효하지 않은 배송 상태",
                    value = """
                        {
                          "code": "D003",
                          "message": "유효하지 않은 배송 상태입니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "A001",
                          "message": "인증이 필요합니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "A002",
                          "message": "접근 권한이 없습니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "배송을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "D001",
                          "message": "배송을 찾을 수 없습니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "중복된 운송장 번호",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "D002",
                          "message": "중복된 운송장 번호입니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> assignTracking(
        @Parameter(description = "배송 ID", required = true, example = "delivery123")
        String deliveryId,
        @Valid AssignTrackingRequest request
    );

    @Operation(
        summary = "배송 취소",
        description = """
            배송을 취소합니다.

            **처리 흐름**
            1. 배송 상태를 CANCELLED로 변경
            2. 취소 사유 기록
            3. 주문 상태도 CANCELLED로 자동 업데이트
            4. 재고 복원
            5. 고객에게 취소 알림

            **제약사항**
            - SHIPPING 상태의 배송만 취소 가능
            - 이미 완료된 배송은 취소 불가
            - 취소 사유 필수

            **취소 사유 예시**: 고객 요청, 배송 불가 지역, 상품 품절, 배송 사고

            **참고사항**: 취소 후에는 다시 배송 시작 불가, 재주문 필요

            **사용 화면**: 관리자 > 배송 관리 > 취소
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "배송 취소 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "AD051",
                          "httpStatus": "OK",
                          "message": "배송 상태 수정 성공",
                          "data": {
                            "id": "delivery123",
                            "orderId": "order123",
                            "trackingNumber": "1234567890123",
                            "carrier": "CJ대한통운",
                            "status": "CANCELLED",
                            "recipientName": "홍길동",
                            "phoneNumber": "01012345678",
                            "zipCode": "12345",
                            "address": "서울시 강남구 테헤란로 123",
                            "deliveryNote": "문 앞에 놓아주세요",
                            "deliveredAt": null,
                            "cancelReason": "고객 요청",
                            "createdAt": "2025-01-16T10:00:00"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "D003",
                          "message": "유효하지 않은 배송 상태입니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "A001",
                          "message": "인증이 필요합니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "A002",
                          "message": "접근 권한이 없습니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "배송을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "D001",
                          "message": "배송을 찾을 수 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> cancelDelivery(
        @Parameter(description = "배송 ID", required = true, example = "delivery123")
        String deliveryId,
        @Valid CancelDeliveryRequest request
    );
}
