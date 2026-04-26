package com.backend.domain.cart.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.cart.dto.request.AddToCartRequest;
import com.backend.domain.cart.dto.request.UpdateCartItemRequest;
import com.backend.domain.cart.dto.response.CartResponse;
import com.backend.global.common.ItemType;
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

@Tag(name = "장바구니", description = "상품 장바구니")
public interface CartApi {

    @Operation(
        summary = "장바구니 조회",
        description = """
            사용자의 장바구니를 조회합니다.

            **응답 정보**
            - items: 장바구니 상품 목록
            - totalAmount: 총 금액
            - totalQuantity: 총 수량
            - empty: 빈 장바구니 여부

            **상품 타입**
            - PRODUCT: 일반 상품
            - CUSTOM_PRODUCT: 맞춤 상품

            **비즈니스 규칙**
            - 재고 부족 상품도 조회됨 (주문 시 재검증)
            - 판매 중지 상품은 표시되지만 주문 불가

            **사용 화면**: 장바구니 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "장바구니 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    name = "장바구니 조회 성공",
                    value = """
                        {
                          "code": "C001",
                          "httpStatus": "OK",
                          "message": "장바구니 조회 성공",
                          "data": {
                            "id": "cart123",
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
                            "totalQuantity": 3,
                            "empty": false
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
    ResponseEntity<SuccessResponse> getCart(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId
    );

    @Operation(
        summary = "장바구니에 상품 추가",
        description = """
            장바구니에 상품을 추가합니다.

            **요청 필드**
            - itemType: PRODUCT / CUSTOM_PRODUCT
            - itemId: 상품 ID
            - quantity: 수량 (1 이상)

            **처리 흐름**
            1. 상품 존재 및 판매 상태 확인
            2. 재고 수량 검증
            3. 이미 담긴 상품이면 수량 증가
            4. 새 상품이면 장바구니에 추가

            **비즈니스 규칙**
            - 재고 초과 시 담기 불가
            - 맞춤 상품은 본인이 신청한 것만 가능
            - 맞춤 상품 유효기간(7일) 확인

            **사용 화면**: 상품 상세 > 장바구니 담기
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "장바구니 추가 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "C002",
                          "httpStatus": "OK",
                          "message": "장바구니 추가 성공",
                          "data": {
                            "id": "cart123",
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
                            "totalQuantity": 3,
                            "empty": false
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
                        name = "재고 부족",
                        value = """
                            {
                              "code": "PR002",
                              "message": "재고가 부족합니다."
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "구매 불가 상품",
                        value = """
                            {
                              "code": "PR007",
                              "message": "구매할 수 없는 상품입니다."
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
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PR001",
                          "message": "상품을 찾을 수 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> addToCart(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Valid AddToCartRequest request
    );

    @Operation(
        summary = "장바구니 상품 수량 변경",
        description = """
            장바구니 상품의 수량을 변경합니다.

            **처리 규칙**
            - quantity = 0: 상품 삭제
            - quantity > 0: 해당 수량으로 변경 (증가/감소 모두 가능)

            **비즈니스 규칙**
            - 재고 초과 수량 설정 불가
            - 맞춤 상품은 추가 수량 허용 여부에 따라 제한

            **사용 화면**: 장바구니 > 수량 조절 버튼
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수량 변경 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "C003",
                          "httpStatus": "OK",
                          "message": "장바구니 수정 성공",
                          "data": {
                            "id": "cart123",
                            "items": [
                              {
                                "itemType": "PRODUCT",
                                "itemId": "prod123",
                                "itemName": "강아지 사료",
                                "unitPrice": 50000,
                                "quantity": 3,
                                "subtotal": 150000,
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
                            "totalAmount": 230000,
                            "totalQuantity": 4,
                            "empty": false
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
                    name = "재고 부족",
                    value = """
                        {
                          "code": "PR002",
                          "message": "재고가 부족합니다."
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
            description = "장바구니 항목을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "C001",
                          "message": "장바구니 항목을 찾을 수 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> updateCartItem(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "상품 타입 (PRODUCT, CUSTOM_PRODUCT)", required = true, example = "PRODUCT")
        ItemType itemType,
        @Parameter(description = "상품 ID", required = true, example = "prod123")
        String itemId,
        @Valid UpdateCartItemRequest request
    );

    @Operation(
        summary = "장바구니 상품 삭제",
        description = """
            장바구니에서 특정 상품을 삭제합니다.

            **식별 정보**
            - itemType: 상품 타입 (PRODUCT / CUSTOM_PRODUCT)
            - itemId: 상품 ID

            **삭제 후 상태**
            - 장바구니가 비면 empty: true 반환
            - totalAmount, totalQuantity 재계산

            **사용 화면**: 장바구니 > 삭제 버튼
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상품 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "C004",
                          "httpStatus": "OK",
                          "message": "장바구니 삭제 성공",
                          "data": {
                            "id": "cart123",
                            "items": [],
                            "totalAmount": 0,
                            "totalQuantity": 0,
                            "empty": true
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
            description = "장바구니 항목을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "C001",
                          "message": "장바구니 항목을 찾을 수 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> removeCartItem(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId,
        @Parameter(description = "상품 타입 (PRODUCT, CUSTOM_PRODUCT)", required = true, example = "PRODUCT")
        ItemType itemType,
        @Parameter(description = "상품 ID", required = true, example = "prod123")
        String itemId
    );

    @Operation(
        summary = "장바구니 비우기",
        description = """
            장바구니의 모든 상품을 삭제합니다.

            **처리 내용**
            - 모든 상품 일괄 삭제
            - 빈 장바구니 상태로 초기화

            **비즈니스 규칙**
            - 이미 빈 장바구니도 성공 처리
            - 삭제된 상품은 복구 불가

            **사용 화면**: 장바구니 > 전체 삭제 버튼
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "장바구니 비우기 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "C005",
                          "httpStatus": "OK",
                          "message": "장바구니 비우기 성공",
                          "data": {
                            "id": "cart123",
                            "items": [],
                            "totalAmount": 0,
                            "totalQuantity": 0,
                            "empty": true
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
    ResponseEntity<SuccessResponse> clearCart(
        @Parameter(description = "사용자 ID", required = true, example = "user123")
        String userId
    );
}
