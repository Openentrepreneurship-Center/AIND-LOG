package com.backend.domain.customproduct.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.customproduct.dto.request.CreateCustomProductRequest;
import com.backend.domain.customproduct.dto.response.CustomProductListResponse;
import com.backend.domain.customproduct.dto.response.CustomProductResponse;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

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

@Tag(name = "맞춤 상품", description = "맞춤 상품 신청")
@SecurityRequirement(name = "cookieAuth")
public interface CustomProductApi {

    @Operation(
            summary = "커스텀 상품 신청",
            description = """
                    일반 카탈로그에 없는 상품을 신청합니다.

                    **비즈니스 로직**
                    - 신청 후 상태는 PENDING으로 시작
                    - 관리자 검토 후 승인/거절 결정
                    - 승인 시 유효기간 내 구매 가능

                    **신청 정보**
                    - 상품명: 원하는 상품의 이름
                    - 상품 설명: 상품에 대한 상세 설명 (선택)
                    - 희망 가격: 신청자가 생각하는 적정 가격
                    - 이미지: 상품 이미지 1장 필수 (JPG, PNG 등)

                    **제약사항**
                    - 이미지는 반드시 1장 첨부 필요

                    **처리 흐름**
                    1. 상품 정보 및 이미지 업로드
                    2. 신청 완료
                    3. 관리자 검토 대기
                    4. 승인 시 장바구니에 추가하여 구매 가능

                    **사용 화면**: 맞춤 상품 신청 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "커스텀 상품 신청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "CP001",
                                      "httpStatus": "CREATED",
                                      "message": "커스텀 상품 신청 성공",
                                      "data": {
                                        "id": "67890abcdef12345",
                                        "userName": "홍길동",
                                        "userEmail": "hong@email.com",
                                        "userPhone": "01012345678",
                                        "petId": "01HXYZ12345",
                                        "petName": "멍멍이",
                                        "petGender": "MALE",
                                        "petBirthdate": "2023-03-15",
                                        "petWeight": 5.2,
                                        "name": "강아지 맞춤 사료",
                                        "description": "우리 강아지를 위한 특별한 사료를 제작하고 싶습니다.",
                                        "requestedPrice": 50000,
                                        "approvedPrice": null,
                                        "weight": 2.5,
                                        "quantity": 3,
                                        "imageUrl": "https://s3.amazonaws.com/decapet/custom-products/12345.jpg",
                                        "status": "PENDING",
                                        "allowMultiple": null,
                                        "additionalInfo": ["원산지: 한국", "성분: 유기농"],
                                        "expirationDate": null,
                                        "createdAt": "2025-12-16T10:00:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 필드 누락, 유효성 검증 실패)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "G001",
                                      "errorMessage": "요청 값이 유효하지 않습니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증되지 않은 사용자입니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "상품 구매 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U007",
                                      "errorMessage": "제품 구매 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
                                    }
                                    """)
                    )
            )
    })
    @RequestBody(
            description = "커스텀 상품 신청 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> createCustomProduct(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "커스텀 상품 신청 정보 (JSON)", required = true)
            @Valid CreateCustomProductRequest request,
            @Parameter(description = "상품 이미지 (JPG, PNG 등, 1장 필수)", required = true)
            MultipartFile image);

    @Operation(
            summary = "내 커스텀 상품 목록 조회",
            description = """
                    본인이 신청한 커스텀 상품 목록을 조회합니다.

                    **비즈니스 로직**
                    - 본인이 신청한 모든 상품 조회 (모든 상태 포함)
                    - PENDING, APPROVED, REJECTED 상태 모두 포함
                    - 기본 페이지 크기: 10
                    - 정렬: 최신순 (createdAt DESC)

                    **응답 정보**
                    - 각 항목은 신청 정보와 현재 상태 포함
                    - APPROVED 상태인 경우 유효기간 정보 확인 가능
                    - 희망 가격, 승인 가격 정보
                    - 수량 추가 가능 여부

                    **사용 화면**: 맞춤 상품 목록 페이지, 마이페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "커스텀 상품 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomProductListResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "CP003",
                                      "httpStatus": "OK",
                                      "message": "커스텀 상품 목록 조회 성공",
                                      "data": {
                                        "items": [
                                          {
                                            "id": "67890abcdef12345",
                                            "userName": "홍길동",
                                            "userEmail": "hong@email.com",
                                            "userPhone": "01012345678",
                                            "petId": "01HXYZ12345",
                                            "petName": "멍멍이",
                                            "petGender": "MALE",
                                            "petBirthdate": "2023-03-15",
                                            "petWeight": 5.2,
                                            "name": "강아지 맞춤 사료",
                                            "description": "우리 강아지를 위한 특별한 사료",
                                            "requestedPrice": 50000,
                                            "approvedPrice": 55000,
                                            "weight": 2.5,
                                            "quantity": 3,
                                            "imageUrl": "https://s3.amazonaws.com/decapet/custom-products/12345.jpg",
                                            "status": "APPROVED",
                                            "allowMultiple": true,
                                            "additionalInfo": ["원산지: 한국", "성분: 유기농"],
                                            "expirationDate": "2026-12-16T10:00:00",
                                            "createdAt": "2025-12-16T10:00:00"
                                          }
                                        ],
                                        "page": 0,
                                        "size": 10,
                                        "totalElements": 1,
                                        "totalPages": 1,
                                        "hasNext": false,
                                        "hasPrevious": false
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증되지 않은 사용자입니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getMyCustomProducts(
            @Parameter(hidden = true) String userId,
            @ParameterObject Pageable pageable);

    @Operation(
            summary = "커스텀 상품 상세 조회",
            description = """
                    커스텀 상품의 상세 정보를 조회합니다.

                    **비즈니스 로직**
                    - 본인이 신청한 커스텀 상품만 조회 가능

                    **응답 정보**
                    - 신청 정보 (이름, 설명, 희망 가격)
                    - 승인 정보 (승인 가격, 수량 추가 가능 여부)
                    - 상태 정보 (PENDING, APPROVED, REJECTED)
                    - 유효기간 정보 (승인일 + 1년)

                    **사용 화면**: 맞춤 상품 상세 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "커스텀 상품 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomProductResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "CP002",
                                      "httpStatus": "OK",
                                      "message": "커스텀 상품 조회 성공",
                                      "data": {
                                        "id": "67890abcdef12345",
                                        "userName": "홍길동",
                                        "userEmail": "hong@email.com",
                                        "userPhone": "01012345678",
                                        "petId": "01HXYZ12345",
                                        "petName": "멍멍이",
                                        "petGender": "MALE",
                                        "petBirthdate": "2023-03-15",
                                        "petWeight": 5.2,
                                        "name": "강아지 맞춤 사료",
                                        "description": "우리 강아지를 위한 특별한 사료를 제작하고 싶습니다.",
                                        "requestedPrice": 50000,
                                        "approvedPrice": 55000,
                                        "weight": 2.5,
                                        "quantity": 3,
                                        "imageUrl": "https://s3.amazonaws.com/decapet/custom-products/12345.jpg",
                                        "status": "APPROVED",
                                        "allowMultiple": true,
                                        "additionalInfo": ["원산지: 한국", "성분: 유기농"],
                                        "expirationDate": "2026-12-16T10:00:00",
                                        "createdAt": "2025-12-16T10:00:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증되지 않은 사용자입니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인의 커스텀 상품이 아님",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "CP006",
                                      "errorMessage": "본인의 커스텀 상품만 구매할 수 있습니다.",
                                      "httpStatus": "FORBIDDEN"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "커스텀 상품을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "CP001",
                                      "errorMessage": "커스텀 상품을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getCustomProduct(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "커스텀 상품 ID", example = "67890abcdef12345", required = true)
            String customProductId);
}
