package com.backend.domain.delivery.controller;

import org.springframework.http.ResponseEntity;

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

@Tag(name = "배송", description = "배송 조회")
public interface DeliveryApi {

    @Operation(
        summary = "주문별 배송 조회",
        description = """
            특정 주문의 배송 정보를 조회합니다.

            **비즈니스 로직**
            - 주문 ID로 배송 정보 조회
            - 배송 상태, 운송장 번호 확인
            - 배송지 정보 확인
            - 배송 완료/취소 일시 확인

            **응답 정보**
            - 배송 ID 및 주문 ID
            - 운송장 번호 및 택배사
            - 배송 상태
            - 수령인 정보 (이름, 전화번호)
            - 배송지 정보 (우편번호, 주소)
            - 배송 메모
            - 배송 완료 일시 (완료된 경우)
            - 취소 사유 (취소된 경우)

            **참고사항**
            - 결제 완료된 주문만 배송 정보가 생성됨
            - 운송장 번호는 관리자가 등록한 후 조회 가능
            - 본인의 주문 배송 정보만 조회 가능

            **사용 화면**: 주문 상세 페이지, 배송 조회 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "배송 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "D001",
                          "httpStatus": "OK",
                          "message": "배송 조회 성공",
                          "data": {
                            "id": "delivery123",
                            "orderId": "order123",
                            "trackingNumber": "1234567890123",
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
            description = "권한 없음 (다른 사용자의 배송 정보)",
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
            description = "배송 정보를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "배송 없음",
                        value = """
                            {
                              "code": "D001",
                              "message": "배송을 찾을 수 없습니다."
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "주문 없음",
                        value = """
                            {
                              "code": "O001",
                              "message": "주문을 찾을 수 없습니다."
                            }
                            """
                    )
                }
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> getDeliveryByOrderId(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "주문 ID", required = true, example = "order123")
        String orderId
    );
}
