package com.backend.domain.order.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.backend.domain.order.dto.request.CreateOrderRequest;
import com.backend.domain.order.entity.OrderStatus;
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

@Tag(name = "주문", description = "주문 관리")
public interface OrderApi {

    @Operation(
        summary = "주문 생성 (장바구니)",
        description = """
            장바구니의 상품으로 주문을 생성합니다.

            **비즈니스 로직**
            - 장바구니 내 모든 상품으로 주문 생성
            - 재고 확인 및 자동 차감
            - 배송 정보 등록
            - 주문 완료 후 장바구니 자동 비우기
            - 자동 생성되는 고유 주문 번호 (ORD-YYYYMMDD-XXXXX)

            **제약사항**
            - 장바구니가 비어있으면 주문 불가
            - 재고가 부족하면 주문 불가
            - 구매 불가 상품이 포함되면 주문 불가
            - 유효한 배송지 정보 필수

            **사용 화면**: 장바구니 페이지, 주문 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "주문 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "O001",
                          "httpStatus": "CREATED",
                          "message": "주문 생성 성공",
                          "data": {
                            "id": "order123",
                            "orderNumber": "ORD-20250116-00001",
                            "items": [
                              {
                                "itemType": "PRODUCT",
                                "itemId": "prod123",
                                "itemName": "강아지 사료",
                                "unitPrice": 50000,
                                "quantity": 2,
                                "subtotal": 100000,
                                "weight": null,
                                "itemQuantity": null,
                                "imageUrl": "https://example.com/image.jpg"
                              },
                              {
                                "itemType": "CUSTOM_PRODUCT",
                                "itemId": "custom456",
                                "itemName": "맞춤형 영양제 세트",
                                "unitPrice": 80000,
                                "quantity": 1,
                                "subtotal": 80000,
                                "weight": 2.5,
                                "itemQuantity": 3,
                                "imageUrl": "https://example.com/custom.jpg"
                              }
                            ],
                            "totalAmount": 180000,
                            "shippingFee": 3000,
                            "grandTotal": 183000,
                            "status": "PENDING",
                            "shippingAddress": {
                              "recipientName": "홍길동",
                              "phoneNumber": "01012345678",
                              "zipCode": "12345",
                              "address": "서울시 강남구",
                              "deliveryNote": "문 앞에 놓아주세요"
                            },
                            "cancellable": true,
                            "cancelReason": null,
                            "trackingNumber": null,
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
                        name = "장바구니 비어있음",
                        value = """
                            {
                              "code": "C002",
                              "message": "장바구니가 비어있습니다."
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "재고 부족",
                        value = """
                            {
                              "code": "PR002",
                              "message": "재고가 부족합니다."
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
            responseCode = "403",
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "U007",
                          "message": "제품 구매 권한이 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> createOrderFromCart(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Valid CreateOrderRequest request
    );

    @Operation(
        summary = "주문 목록 조회",
        description = """
            사용자의 전체 주문 목록을 조회합니다.

            **비즈니스 로직**
            - 본인의 모든 주문 조회
            - 페이징 지원
            - 최신 주문 순으로 정렬

            **응답 정보**
            - 주문 번호, 상태, 금액 정보
            - 주문 일시
            - 간략한 상품 정보

            **사용 화면**: 주문 목록 페이지, 마이페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "O003",
                          "httpStatus": "OK",
                          "message": "주문 목록 조회 성공",
                          "data": {
                            "content": [
                              {
                                "id": "order123",
                                "orderNumber": "ORD-20250116-00001",
                                "status": "PENDING",
                                "grandTotal": 103000,
                                "itemCount": 2,
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
    ResponseEntity<SuccessResponse> getOrders(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "주문 상태별 목록 조회",
        description = """
            특정 상태의 주문 목록을 조회합니다.

            **비즈니스 로직**
            - 주문 상태별 필터링
            - 페이징 지원
            - 최신 주문 순으로 정렬

            **주문 상태**
            - PENDING: 결제 대기 중
            - CONFIRMED: 주문 확정 (결제 완료)
            - SHIPPED: 발송 완료
            - CANCELLED: 주문 취소

            **사용 예시**
            - 결제 대기 중인 주문만 조회
            - 배송 완료된 주문만 조회

            **사용 화면**: 주문 목록 페이지, 마이페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "O003",
                          "httpStatus": "OK",
                          "message": "주문 목록 조회 성공",
                          "data": {
                            "content": [
                              {
                                "id": "order123",
                                "orderNumber": "ORD-20250116-00001",
                                "status": "PENDING",
                                "grandTotal": 103000,
                                "itemCount": 2,
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
    ResponseEntity<SuccessResponse> getOrdersByStatus(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "주문 상태 (PENDING, CONFIRMED, SHIPPED, CANCELLED)", required = true, example = "PENDING")
        OrderStatus status,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "주문 상세 조회",
        description = """
            특정 주문의 상세 정보를 조회합니다.

            **비즈니스 로직**
            - 주문의 모든 상세 정보 조회
            - 주문 상품 목록
            - 배송지 정보
            - 취소 가능 여부 확인

            **응답 정보**
            - 주문 번호, 상태
            - 상품 목록 (상품명, 가격, 수량)
            - 금액 정보 (상품 금액, 배송비, 총 금액)
            - 배송지 정보
            - 취소 가능 여부 및 취소 사유 (취소된 경우)

            **사용 화면**: 주문 상세 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "O002",
                          "httpStatus": "OK",
                          "message": "주문 조회 성공",
                          "data": {
                            "id": "order123",
                            "orderNumber": "ORD-20250116-00001",
                            "items": [
                              {
                                "itemType": "PRODUCT",
                                "itemId": "prod123",
                                "itemName": "강아지 사료",
                                "unitPrice": 50000,
                                "quantity": 2,
                                "subtotal": 100000,
                                "weight": null,
                                "itemQuantity": null,
                                "imageUrl": "https://example.com/image.jpg"
                              },
                              {
                                "itemType": "CUSTOM_PRODUCT",
                                "itemId": "custom456",
                                "itemName": "맞춤형 영양제 세트",
                                "unitPrice": 80000,
                                "quantity": 1,
                                "subtotal": 80000,
                                "weight": 2.5,
                                "itemQuantity": 3,
                                "imageUrl": "https://example.com/custom.jpg"
                              }
                            ],
                            "totalAmount": 180000,
                            "shippingFee": 3000,
                            "grandTotal": 183000,
                            "status": "PENDING",
                            "shippingAddress": {
                              "recipientName": "홍길동",
                              "phoneNumber": "01012345678",
                              "zipCode": "12345",
                              "address": "서울시 강남구",
                              "deliveryNote": "문 앞에 놓아주세요"
                            },
                            "cancellable": true,
                            "cancelReason": null,
                            "trackingNumber": null,
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
            description = "주문을 찾을 수 없음",
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
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> getOrder(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "주문 ID", required = true, example = "order123")
        String orderId
    );

    @Operation(
        summary = "주문 취소",
        description = """
            주문을 취소합니다.

            **비즈니스 로직**
            - 결제 대기 중인 주문 취소
            - 재고 자동 복원
            - 취소 사유 기록
            - 주문 상태가 CANCELLED로 변경

            **제약사항**
            - PENDING 상태의 주문만 취소 가능
            - 이미 결제 완료된 주문은 취소 불가 (환불 절차 필요)
            - 배송이 시작된 주문은 취소 불가

            **취소 후 처리**
            - 차감되었던 재고가 복원됨
            - 취소 사유가 기록됨

            **참고사항**
            - 결제 완료 후 취소는 별도 환불 절차 필요
            - 배송 시작 후에는 반품/교환 절차 이용

            **사용 화면**: 주문 상세 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 취소 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "O004",
                          "httpStatus": "OK",
                          "message": "주문 취소 성공",
                          "data": {
                            "id": "order123",
                            "orderNumber": "ORD-20250116-00001",
                            "status": "CANCELLED",
                            "cancellable": false,
                            "cancelReason": "단순 변심",
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
                        name = "취소 불가 주문",
                        value = """
                            {
                              "code": "O004",
                              "message": "취소할 수 없는 주문입니다."
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "유효하지 않은 상태",
                        value = """
                            {
                              "code": "O003",
                              "message": "유효하지 않은 주문 상태입니다."
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
            description = "주문을 찾을 수 없음",
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
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> cancelOrder(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "주문 ID", required = true, example = "order123")
        String orderId,
        @Parameter(description = "취소 사유", required = true, example = "단순 변심")
        String reason
    );
}
