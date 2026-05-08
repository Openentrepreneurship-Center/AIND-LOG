package com.backend.domain.customproduct.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.backend.domain.customproduct.dto.request.ApproveCustomProductRequest;
import com.backend.domain.customproduct.dto.request.UpdateCustomProductRequest;
import com.backend.domain.customproduct.dto.response.CustomProductListResponse;
import com.backend.domain.customproduct.dto.response.CustomProductResponse;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

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

@Tag(name = "[Admin] 맞춤 상품", description = "맞춤 상품 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminCustomProductApi {

    @Operation(
            summary = "대기 중인 커스텀 상품 목록 조회",
            description = """
                    승인 대기 중인 커스텀 상품 신청 목록을 조회합니다.

                    **조회 범위**
                    - 상태가 PENDING인 신청만 조회
                    - 신청일시 최신순 정렬

                    **페이징**
                    - 기본 페이지 크기: 10
                    - 정렬: 최신순 (createdAt DESC)

                    **사용 화면**: 관리자 > 맞춤 상품 관리 > 대기 목록
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "대기 중인 커스텀 상품 목록 조회 성공",
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
                                        ],
                                        "page": 0,
                                        "size": 10,
                                        "totalElements": 5,
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A002",
                                      "errorMessage": "접근 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getPendingCustomProducts(@ParameterObject Pageable pageable);

    @Operation(
            summary = "전체 커스텀 상품 목록 조회",
            description = """
                    전체 커스텀 상품 신청 목록을 조회합니다.

                    **조회 범위**
                    - 모든 상태(PENDING, APPROVED, REJECTED)의 신청 포함
                    - 신청일시 최신순 정렬

                    **페이징**
                    - 기본 페이지 크기: 10
                    - 정렬: 최신순 (createdAt DESC)

                    **사용 화면**: 관리자 > 맞춤 상품 관리 > 전체 목록
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 커스텀 상품 목록 조회 성공",
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
                                          },
                                          {
                                            "id": "67890abcdef12346",
                                            "userName": "김철수",
                                            "userEmail": "kim@email.com",
                                            "userPhone": "01098765432",
                                            "petId": "01HXYZ67890",
                                            "petName": "야옹이",
                                            "petGender": "FEMALE",
                                            "petBirthdate": "2022-05-20",
                                            "petWeight": 4.0,
                                            "name": "고양이 간식",
                                            "description": "우리 고양이가 좋아하는 간식",
                                            "requestedPrice": 30000,
                                            "approvedPrice": null,
                                            "weight": 2.5,
                                            "quantity": 3,
                                            "imageUrl": "https://s3.amazonaws.com/decapet/custom-products/12346.jpg",
                                            "status": "REJECTED",
                                            "allowMultiple": null,
                                            "additionalInfo": null,
                                            "expirationDate": null,
                                            "createdAt": "2025-12-15T14:00:00"
                                          }
                                        ],
                                        "page": 0,
                                        "size": 10,
                                        "totalElements": 45,
                                        "totalPages": 5,
                                        "hasNext": true,
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A002",
                                      "errorMessage": "접근 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getAllCustomProducts(
            @RequestParam(required = false) String searchText,
            @ParameterObject Pageable pageable);

    @Operation(
            summary = "커스텀 상품 상세 조회",
            description = """
                    커스텀 상품 신청의 상세 정보를 조회합니다.

                    **조회 정보**
                    - 신청자 정보
                    - 상품 상세 정보 (이름, 설명, 이미지)
                    - 가격 정보 (희망 가격, 승인 가격)
                    - 상태 정보
                    - 유효기간 정보 (승인된 경우)

                    **사용 화면**: 관리자 > 맞춤 상품 관리 > 상세
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "커스텀 상품 상세 조회 성공",
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
                                        "description": "우리 강아지를 위한 특별한 사료를 제작하고 싶습니다. 알레르기가 있어서 일반 사료를 먹을 수 없습니다.",
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
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A002",
                                      "errorMessage": "접근 권한이 없습니다.",
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
            @Parameter(description = "커스텀 상품 ID", example = "67890abcdef12345", required = true)
            String customProductId);

    @Operation(
            summary = "커스텀 상품 수정",
            description = "커스텀 상품의 정보를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "커스텀 상품을 찾을 수 없음")
    })
    ResponseEntity<SuccessResponse> updateCustomProduct(
            @Parameter(description = "커스텀 상품 ID", required = true)
            String customProductId,
            @Valid UpdateCustomProductRequest request);

    @Operation(
            summary = "커스텀 상품 승인",
            description = """
                    커스텀 상품 신청을 승인합니다.

                    **처리 흐름**
                    1. 최종 가격 결정 (희망 가격 참고, 원가/제작 비용 고려)
                    2. 수량 추가 가능 여부 결정 (true: 수량 증가 허용, false: 신청 수량만)
                    3. 유효기간 자동 설정 (승인 시점으로부터 1년)
                    4. 상태를 APPROVED로 변경
                    5. 신청자에게 알림 발송

                    **승인 조건**
                    - PENDING 상태의 신청만 승인 가능
                    - 승인 후에는 취소 불가

                    **사용 화면**: 관리자 > 맞춤 상품 관리 > 승인
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "커스텀 상품 승인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "CP004",
                                      "httpStatus": "OK",
                                      "message": "커스텀 상품 승인 성공",
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
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 승인되었거나 잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "이미 승인됨", value = """
                                            {
                                              "code": "CP002",
                                              "errorMessage": "이미 승인된 신청입니다.",
                                              "httpStatus": "BAD_REQUEST"
                                            }
                                            """),
                                    @ExampleObject(name = "유효성 검증 실패", value = """
                                            {
                                              "code": "G001",
                                              "errorMessage": "요청 값이 유효하지 않습니다.",
                                              "httpStatus": "BAD_REQUEST"
                                            }
                                            """)
                            }
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
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A002",
                                      "errorMessage": "접근 권한이 없습니다.",
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
    ResponseEntity<SuccessResponse> approveCustomProduct(
            @Parameter(description = "커스텀 상품 ID", example = "67890abcdef12345", required = true)
            String customProductId,
            @Parameter(description = "승인 정보 (최종 가격, 수량 추가 가능 여부)", required = true)
            @Valid ApproveCustomProductRequest request);

    @Operation(
            summary = "커스텀 상품 거절",
            description = """
                    커스텀 상품 신청을 거절합니다.

                    **거절 사유 예시**
                    - 부적절한 상품 내용
                    - 제작 불가능한 상품
                    - 가격 협의 불가
                    - 기타 사유

                    **처리 흐름**
                    1. 상태를 REJECTED로 변경
                    2. 신청자에게 알림 발송
                    3. 재신청 가능 (새로운 신청으로)

                    **거절 조건**
                    - PENDING 상태의 신청만 거절 가능
                    - 거절 후에는 취소 불가

                    **사용 화면**: 관리자 > 맞춤 상품 관리 > 거절
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "커스텀 상품 거절 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "CP005",
                                      "httpStatus": "OK",
                                      "message": "커스텀 상품 거절 성공",
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
                                        "description": "우리 강아지를 위한 특별한 사료",
                                        "requestedPrice": 50000,
                                        "approvedPrice": null,
                                        "weight": 2.5,
                                        "quantity": 3,
                                        "imageUrl": "https://s3.amazonaws.com/decapet/custom-products/12345.jpg",
                                        "status": "REJECTED",
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
                    description = "이미 거절되었거나 잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "CP003",
                                      "errorMessage": "이미 거절된 신청입니다.",
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
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A002",
                                      "errorMessage": "접근 권한이 없습니다.",
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
    ResponseEntity<SuccessResponse> rejectCustomProduct(
            @Parameter(description = "커스텀 상품 ID", example = "67890abcdef12345", required = true)
            String customProductId);

    @Operation(
            summary = "커스텀 상품 상태 복원 (PENDING으로)",
            description = """
                    커스텀 상품 상태를 PENDING으로 복원합니다.
                    승인 가격, 승인일시, 수량 추가 가능 여부가 초기화됩니다.

                    **사용 화면**: 관리자 > 맞춤 상품 관리 > 상태 복원
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 복원 성공"),
            @ApiResponse(responseCode = "404", description = "커스텀 상품을 찾을 수 없음")
    })
    ResponseEntity<SuccessResponse> revertCustomProduct(
            @Parameter(description = "커스텀 상품 ID", example = "67890abcdef12345", required = true)
            String customProductId);

    @Operation(summary = "맞춤 상품 삭제", description = "맞춤 상품을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    ResponseEntity<SuccessResponse> deleteCustomProduct(
            @Parameter(description = "커스텀 상품 ID", example = "67890abcdef12345", required = true)
            String customProductId);
}
