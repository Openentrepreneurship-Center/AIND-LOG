package com.backend.domain.product.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.backend.global.common.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "상품", description = "상품 조회")
public interface ProductApi {

    @Operation(
        summary = "메인 페이지 추천 상품 조회",
        description = """
            메인 페이지에 표시할 추천 상품을 조회합니다.

            **인증**: 불필요 (공개 API)

            **응답 정보**
            - 활성화된 상품 중 랜덤 4개 반환
            - 상품 ID, 이름, 원가, 판매가, 유통기한, 이미지 URL

            **사용 화면**: 메인 페이지 > 추천 상품 섹션
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "추천 상품 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PR001",
                          "httpStatus": "OK",
                          "message": "상품 조회 성공",
                          "data": [
                            {
                              "id": "507f1f77bcf86cd799439011",
                              "name": "프리미엄 강아지 사료",
                              "basePrice": 50000,
                              "price": 45000,
                              "expirationDate": "2025-12-31",
                              "imageUrl": "https://example.com/product1.jpg"
                            },
                            {
                              "id": "507f1f77bcf86cd799439012",
                              "name": "고양이 간식 세트",
                              "basePrice": 30000,
                              "price": 27000,
                              "expirationDate": "2026-06-30",
                              "imageUrl": "https://example.com/product2.jpg"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "G002",
                          "errorMessage": "서버 오류가 발생했습니다.",
                          "httpStatus": "INTERNAL_SERVER_ERROR"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<SuccessResponse> getFeaturedProducts();

    @Operation(
        summary = "상품 목록 조회",
        description = """
            판매 가능한 상품 목록을 조회합니다.

            **인증**: 불필요 (공개 API)

            **페이지네이션**
            - page: 페이지 번호 (0부터 시작)
            - size: 페이지 크기 (기본값: 20)
            - sort: 정렬 기준 (예: createdAt,desc / price,asc)

            **응답 정보**
            - products: 상품 요약 목록
            - page/size/totalElements/totalPages
            - hasNext/hasPrevious

            **필터링**: available=true인 상품만 조회

            **사용 화면**: 상품 목록 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상품 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PR002",
                          "httpStatus": "OK",
                          "message": "상품 목록 조회 성공",
                          "data": {
                            "products": [
                              {
                                "id": "507f1f77bcf86cd799439011",
                                "name": "프리미엄 강아지 사료",
                                "basePrice": 50000,
                                "price": 45000,
                                "expirationDate": "2025-12-31",
                                "imageUrl": "https://example.com/product1.jpg"
                              }
                            ],
                            "page": 0,
                            "size": 20,
                            "totalElements": 45,
                            "totalPages": 3,
                            "hasNext": true,
                            "hasPrevious": false
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 페이지네이션 파라미터",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "G001",
                          "errorMessage": "요청 값이 유효하지 않습니다.",
                          "httpStatus": "BAD_REQUEST"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<SuccessResponse> getProducts(
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "상품 상세 조회",
        description = """
            특정 상품의 상세 정보를 조회합니다.

            **인증**: 불필요 (공개 API)

            **응답 정보**
            - 기본 정보: ID, 이름, 설명
            - 가격 정보: 원가(basePrice), 판매가(price)
            - 상품 정보: weight, expirationDate, imageUrl

            **사용 화면**: 상품 상세 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상품 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PR001",
                          "httpStatus": "OK",
                          "message": "상품 조회 성공",
                          "data": {
                            "id": "507f1f77bcf86cd799439011",
                            "name": "프리미엄 강아지 사료",
                            "description": "모든 연령대의 강아지에게 적합한 프리미엄 사료입니다.",
                            "basePrice": 50000,
                            "price": 45000,
                            "weight": 3.0,
                            "expirationDate": "2025-12-31",
                            "imageUrl": "https://example.com/product1.jpg"
                          }
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
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "PR001",
                          "errorMessage": "상품을 찾을 수 없습니다.",
                          "httpStatus": "NOT_FOUND"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<SuccessResponse> getProduct(
        @Parameter(description = "조회할 상품 ID", example = "507f1f77bcf86cd799439011", required = true)
        String productId
    );
}
