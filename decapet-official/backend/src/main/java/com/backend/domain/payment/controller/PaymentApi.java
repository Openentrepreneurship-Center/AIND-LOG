package com.backend.domain.payment.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.backend.domain.payment.dto.request.CompletePaymentRequest;
import com.backend.domain.payment.dto.request.CreatePaymentRequest;
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

@Tag(name = "결제", description = "결제 처리")
public interface PaymentApi {

    @Operation(
        summary = "결제 요청",
        description = """
            주문 또는 승인된 예약에 대해 결제를 요청합니다.

            **비즈니스 로직**
            - 주문(ORDER) 또는 예약(APPOINTMENT) 결제 생성
            - 결제 금액 자동 계산
            - 중복 결제 방지
            - PG사 연동 준비

            **제약사항**
            - 주문: PENDING 상태의 주문만 결제 가능
            - 예약: APPROVED 상태의 예약만 결제 가능
            - 이미 결제가 진행 중이면 생성 불가
            - 본인의 주문/예약만 결제 가능

            **결제 수단**
            - CARD: 신용카드
            - TRANSFER: 계좌이체
            - VIRTUAL_ACCOUNT: 가상계좌
            - MOBILE: 휴대폰 소액결제

            **처리 흐름**
            1. 응답으로 받은 결제 ID와 금액 확인
            2. PG사 결제 페이지로 이동하여 결제 진행
            3. 결제 완료 후 completePayment API 호출

            **사용 화면**: 주문 페이지, 예약 결제 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "결제 요청 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM001",
                          "httpStatus": "CREATED",
                          "message": "결제 요청 성공",
                          "data": {
                            "id": "payment123",
                            "paymentType": "ORDER",
                            "referenceId": "order123",
                            "amount": 103000,
                            "status": "PENDING",
                            "paymentMethod": "CARD",
                            "pgTransactionId": null,
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
                examples = {
                    @ExampleObject(
                        name = "결제 대기 중 아님",
                        value = """
                            {
                              "code": "O005",
                              "message": "결제 대기 중인 주문이 아닙니다."
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "예약 미승인",
                        value = """
                            {
                              "code": "PM005",
                              "message": "승인된 예약만 결제할 수 있습니다."
                            }
                            """
                    )
                }
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
            responseCode = "404",
            description = "주문/예약을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "O001",
                          "message": "주문을 찾을 수 없습니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 결제 진행 중",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM002",
                          "message": "이미 결제가 진행 중입니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> createPayment(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Valid CreatePaymentRequest request
    );

    @Operation(
        summary = "결제 완료",
        description = """
            결제를 완료 처리합니다.

            **비즈니스 로직**
            - PG사 결제 완료 후 호출
            - PG 거래 ID 저장
            - 결제 상태를 COMPLETED로 변경
            - 주문/예약 상태 자동 업데이트
              - 주문: PENDING -> CONFIRMED
              - 예약: APPROVED -> PAID

            **제약사항**
            - PENDING 상태의 결제만 완료 처리 가능
            - PG 거래 ID 필수
            - 본인의 결제만 완료 가능

            **자동 처리**
            - 주문 확정
            - 배송 정보 생성
            - 예약 결제 완료 처리

            **실패 시**
            - 결제 상태는 PENDING으로 유지
            - 주문/예약 상태는 변경되지 않음
            - 재시도 가능

            **사용 화면**: 결제 페이지 (PG사 결제 후 콜백)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "결제 완료 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM002",
                          "httpStatus": "OK",
                          "message": "결제 완료",
                          "data": {
                            "id": "payment123",
                            "paymentType": "ORDER",
                            "referenceId": "order123",
                            "amount": 103000,
                            "status": "COMPLETED",
                            "paymentMethod": "CARD",
                            "pgTransactionId": "PG20250116123456",
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
                examples = {
                    @ExampleObject(
                        name = "결제 대기 중 아님",
                        value = """
                            {
                              "code": "PM003",
                              "message": "대기 중인 결제가 아닙니다."
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "결제 실패",
                        value = """
                            {
                              "code": "PM004",
                              "message": "결제에 실패했습니다."
                            }
                            """
                    )
                }
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
            responseCode = "404",
            description = "결제를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM001",
                          "message": "결제 정보를 찾을 수 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> completePayment(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "결제 ID", required = true, example = "payment123")
        String paymentId,
        @Valid CompletePaymentRequest request
    );

    @Operation(
        summary = "결제 취소",
        description = """
            대기 중인 결제를 취소합니다.

            **비즈니스 로직**
            - PENDING 상태의 결제 취소
            - 결제 상태를 CANCELLED로 변경
            - 주문/예약 상태는 유지됨

            **제약사항**
            - PENDING 상태의 결제만 취소 가능
            - 이미 완료된 결제는 취소 불가 (환불 절차 필요)
            - 본인의 결제만 취소 가능

            **참고사항**
            - 결제 취소 시 주문/예약은 취소되지 않음
            - 주문 취소가 필요한 경우 별도로 주문 취소 API 호출
            - 완료된 결제의 환불은 관리자 문의 필요

            **사용 화면**: 결제 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "결제 취소 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM003",
                          "httpStatus": "OK",
                          "message": "결제 취소 성공",
                          "data": {
                            "id": "payment123",
                            "paymentType": "ORDER",
                            "referenceId": "order123",
                            "amount": 103000,
                            "status": "CANCELLED",
                            "paymentMethod": "CARD",
                            "pgTransactionId": null,
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
                          "code": "PM003",
                          "message": "대기 중인 결제가 아닙니다."
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
            responseCode = "404",
            description = "결제를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM001",
                          "message": "결제 정보를 찾을 수 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> cancelPayment(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "결제 ID", required = true, example = "payment123")
        String paymentId
    );

    @Operation(
        summary = "내 결제 목록 조회",
        description = """
            내 결제 목록을 조회합니다.

            **비즈니스 로직**
            - 본인의 모든 결제 내역 조회
            - 페이징 지원
            - 최신 결제 순으로 정렬

            **응답 정보**
            - 결제 ID, 타입, 상태
            - 결제 금액, 결제 수단
            - PG 거래 ID (완료된 경우)
            - 결제 일시

            **사용 화면**: 결제 목록 페이지, 마이페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "결제 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM005",
                          "httpStatus": "OK",
                          "message": "결제 목록 조회 성공",
                          "data": {
                            "content": [
                              {
                                "id": "payment123",
                                "paymentType": "ORDER",
                                "referenceId": "order123",
                                "amount": 103000,
                                "status": "COMPLETED",
                                "paymentMethod": "CARD",
                                "pgTransactionId": "PG20250116123456",
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
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> getMyPayments(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "결제 상세 조회",
        description = """
            결제 상세 정보를 조회합니다.

            **비즈니스 로직**
            - 특정 결제의 상세 정보 조회
            - 결제 타입별 참조 정보 확인
            - PG 거래 정보 확인

            **응답 정보**
            - 결제 기본 정보 (ID, 타입, 상태, 금액)
            - 결제 수단 및 PG 거래 ID
            - 참조 ID (주문 ID 또는 예약 ID)
            - 결제 일시

            **사용 화면**: 결제 상세 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "결제 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM004",
                          "httpStatus": "OK",
                          "message": "결제 조회 성공",
                          "data": {
                            "id": "payment123",
                            "paymentType": "ORDER",
                            "referenceId": "order123",
                            "amount": 103000,
                            "status": "COMPLETED",
                            "paymentMethod": "CARD",
                            "pgTransactionId": "PG20250116123456",
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
            responseCode = "404",
            description = "결제를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PM001",
                          "message": "결제 정보를 찾을 수 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> getPayment(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "결제 ID", required = true, example = "payment123")
        String paymentId
    );
}
