package com.backend.domain.product.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.product.dto.request.CreateProductRequest;
import com.backend.domain.product.dto.request.UpdateProductRequest;
import com.backend.global.common.SuccessResponse;

import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[Admin] 상품", description = "상품 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminProductApi {

    @Operation(
        summary = "상품 생성",
        description = """
            새 상품을 등록합니다.

            **요청 형식**: multipart/form-data
            - request: 상품 정보 (JSON)
            - image: 상품 이미지 (필수)

            **처리 흐름**
            1. 이미지 업로드 (S3)
            2. 상품 정보 저장 (PostgreSQL)
            3. 상품 ID 반환

            **제약사항**
            - 상품명: 필수, 공백 불가
            - 원가/판매가: 필수, 0보다 커야 함
            - 재고: 필수, 0 이상

            **사용 화면**: 관리자 > 상품 관리 > 등록
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "상품 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "AD100",
                          "httpStatus": "CREATED",
                          "message": "생성 성공",
                          "data": {
                            "id": "507f1f77bcf86cd799439011",
                            "name": "프리미엄 강아지 사료",
                            "description": "모든 연령대의 강아지에게 적합한 프리미엄 사료입니다.",
                            "basePrice": 50000,
                            "price": 45000,
                            "weight": 3.0,
                            "stockQuantity": 100,
                            "expirationDate": "2025-12-31",
                            "imageUrl": "https://s3.amazonaws.com/bucket/product1.jpg",
                            "available": true,
                            "createdAt": "2025-01-15T09:30:00"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "유효성 검증 실패 또는 이미지 누락",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = {
                    @ExampleObject(
                        name = "유효성 검증 실패",
                        value = """
                            {
                              "code": "G001",
                              "errorMessage": "요청 값이 유효하지 않습니다.",
                              "httpStatus": "BAD_REQUEST"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "이미지 필수",
                        value = """
                            {
                              "code": "PR006",
                              "errorMessage": "이미지는 필수입니다.",
                              "httpStatus": "BAD_REQUEST"
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
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "A001",
                          "errorMessage": "인증이 필요합니다.",
                          "httpStatus": "UNAUTHORIZED"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (관리자 아님)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "A002",
                          "errorMessage": "접근 권한이 없습니다.",
                          "httpStatus": "FORBIDDEN"
                        }
                        """
                )
            )
        )
    })
    @RequestBody(
            description = "상품 생성 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> createProduct(
        @Valid
        @Parameter(description = "상품 생성 요청 정보 (JSON)", required = true)
        CreateProductRequest request,

        @Parameter(description = "상품 이미지 파일 (필수)", required = true)
        MultipartFile image
    );

    @Operation(
        summary = "상품 상세 조회",
        description = """
            특정 상품의 상세 정보를 조회합니다.

            **조회 정보**
            - 상품 기본 정보 (이름, 설명, 가격, 이미지)
            - 재고 현황
            - 활성/비활성 상태
            - 생성일 및 수정일

            **권한**: 관리자는 활성/비활성 상태와 관계없이 모든 상품 조회 가능

            **사용 화면**: 관리자 > 상품 관리 > 상세
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
                            "stockQuantity": 100,
                            "expirationDate": "2025-12-31",
                            "imageUrl": "https://s3.amazonaws.com/bucket/product1.jpg",
                            "available": true,
                            "createdAt": "2025-01-15T09:30:00"
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

    @Operation(
        summary = "상품 목록 조회 (관리자용)",
        description = """
            전체 상품 목록을 조회합니다.

            **조회 범위**
            - 활성/비활성 상태 모두 포함
            - 재고 현황, 판매 상태 등 관리자용 정보 포함

            **페이지네이션**
            - page: 페이지 번호 (0부터 시작, 기본값: 0)
            - size: 페이지 크기 (기본값: 20)
            - sort: 정렬 기준 (예: createdAt,desc, name,asc, stockQuantity,asc)

            **사용 화면**: 관리자 > 상품 관리 > 목록
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
                                "imageUrl": "https://s3.amazonaws.com/bucket/product1.jpg"
                              }
                            ],
                            "page": 0,
                            "size": 20,
                            "totalElements": 150,
                            "totalPages": 8,
                            "hasNext": true,
                            "hasPrevious": false
                          }
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<SuccessResponse> getAllProducts(
        @RequestParam(required = false) String searchText,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "상품 수정",
        description = """
            상품 정보를 수정합니다.

            **요청 형식**: multipart/form-data
            - request: 상품 정보 (JSON)
            - image: 상품 이미지 (선택, 제공 시 기존 이미지 교체)

            **수정 가능 항목**
            - 상품명, 설명, 가격, 재고, 유통기한, 이미지

            **처리 흐름**
            1. 상품 조회
            2. 이미지 제공 시 S3 업로드
            3. 상품 정보 업데이트

            **사용 화면**: 관리자 > 상품 관리 > 수정
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상품 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "AD101",
                          "httpStatus": "OK",
                          "message": "수정 성공",
                          "data": {
                            "id": "507f1f77bcf86cd799439011",
                            "name": "프리미엄 강아지 사료 (수정됨)",
                            "description": "업데이트된 설명입니다.",
                            "basePrice": 55000,
                            "price": 49500,
                            "weight": 3.0,
                            "stockQuantity": 80,
                            "expirationDate": "2025-12-31",
                            "imageUrl": "https://s3.amazonaws.com/bucket/product1-updated.jpg",
                            "available": true,
                            "createdAt": "2025-01-15T09:30:00"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "유효성 검증 실패",
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
    @RequestBody(
            description = "상품 수정 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> updateProduct(
        @Parameter(description = "수정할 상품 ID", example = "507f1f77bcf86cd799439011", required = true)
        String productId,

        @Valid
        @Parameter(description = "상품 수정 요청 정보 (JSON)", required = true)
        UpdateProductRequest request,

        @Parameter(description = "새 상품 이미지 파일 (선택사항, 제공 시 기존 이미지 교체)", required = false)
        MultipartFile image
    );

    @Operation(
        summary = "상품 삭제",
        description = """
            상품을 삭제합니다 (소프트 삭제).

            **처리 방식**
            - 실제 데이터는 삭제되지 않음
            - available 플래그가 false로 변경
            - 이미 주문된 상품의 이력은 유지

            **삭제 효과**
            - 상품 목록에서 제외됨
            - 구매 불가능
            - 관리자 페이지에서는 조회 가능

            **사용 화면**: 관리자 > 상품 관리 > 삭제
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
                          "code": "AD102",
                          "httpStatus": "OK",
                          "message": "삭제 성공",
                          "data": null
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
    ResponseEntity<SuccessResponse> deleteProduct(
        @Parameter(description = "삭제할 상품 ID", example = "507f1f77bcf86cd799439011", required = true)
        String productId
    );
}
