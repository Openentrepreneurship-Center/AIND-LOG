package com.backend.domain.prescription.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.prescription.dto.request.PrescriptionApproveRequest;
import com.backend.domain.prescription.entity.PrescriptionStatus;
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

@Tag(name = "[Admin] 처방", description = "처방 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminPrescriptionApi {

    @Operation(
            summary = "처방전 목록 조회",
            description = """
                    모든 처방전 목록을 조회합니다.

                    **필터링**
                    - PENDING: 승인 대기
                    - APPROVED: 승인됨 (결제 대기)
                    - REJECTED: 거절됨

                    **정렬 및 페이징**
                    - 기본 정렬: 최신순 (createdAt DESC)
                    - 페이지 크기 기본값: 20

                    **응답 정보**
                    - 처방전 ID, 사용자 및 반려동물 정보
                    - 타임슬롯 정보, 이미지 URL 목록
                    - 처방전 상태, 결제 금액 및 생성 일시

                    **사용 화면**: 관리자 > 처방전 관리 > 목록
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "처방전 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX003",
                                      "httpStatus": "OK",
                                      "message": "처방전 목록 조회 성공",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "550e8400-e29b-41d4-a716-446655440000",
                                            "timeSlotId": "timeslot123",
                                            "userId": "user123",
                                            "petId": "pet123",
                                            "petName": "멍멍이",
                                            "imageUrls": [
                                              "https://s3.amazonaws.com/bucket/prescription1.jpg"
                                            ],
                                            "description": "감기약 처방전",
                                            "status": "PENDING",
                                            "price": null,
                                            "createdAt": "2025-12-16T10:30:00"
                                          }
                                        ],
                                        "pageable": {
                                          "pageNumber": 0,
                                          "pageSize": 20
                                        },
                                        "totalElements": 1,
                                        "totalPages": 1
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (관리자 인증 필요)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증에 실패했습니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (관리자 권한 필요)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "G002",
                                      "errorMessage": "접근 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getPrescriptions(
            @Parameter(description = "처방전 상태 필터 (선택사항)", example = "PENDING") PrescriptionStatus status,
            @Parameter(description = "검색 키워드 (보호자 이름/전화번호)") String keyword,
            @ParameterObject Pageable pageable);

    @Operation(
            summary = "처방전 상세 조회",
            description = """
                    처방전의 상세 정보를 조회합니다.

                    **응답 정보**
                    - 처방전 기본 정보 (ID, 상태, 생성일시)
                    - 사용자 및 반려동물 정보
                    - 타임슬롯 정보
                    - 업로드된 처방전 이미지 URL 목록
                    - 설명 및 결제 금액

                    **권한**: 관리자는 모든 처방전 조회 가능

                    **사용 화면**: 관리자 > 처방전 관리 > 상세
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "처방전 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX002",
                                      "httpStatus": "OK",
                                      "message": "처방전 조회 성공",
                                      "data": {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "timeSlotId": "timeslot123",
                                        "userId": "user123",
                                        "petId": "pet123",
                                        "petName": "멍멍이",
                                        "imageUrls": [
                                          "https://s3.amazonaws.com/bucket/prescription1.jpg",
                                          "https://s3.amazonaws.com/bucket/prescription2.jpg"
                                        ],
                                        "description": "감기약 및 영양제 처방전입니다.",
                                        "status": "PENDING",
                                        "price": null,
                                        "createdAt": "2025-12-16T10:30:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "처방전을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX001",
                                      "errorMessage": "처방전을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getPrescription(
            @Parameter(description = "처방전 ID", example = "550e8400-e29b-41d4-a716-446655440000") String prescriptionId);

    @Operation(
            summary = "처방전 승인",
            description = """
                    처방전을 승인하고 결제 금액을 설정합니다.

                    **승인 조건**
                    - 처방전 상태가 PENDING이어야 함
                    - 이미 승인되거나 거절된 처방전은 승인 불가

                    **처리 흐름**
                    1. 처방전 상태를 APPROVED로 변경
                    2. 결제 금액 설정
                    3. 사용자가 결제 가능 상태가 됨

                    **사용 화면**: 관리자 > 처방전 관리 > 승인
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "처방전 승인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD040",
                                      "httpStatus": "OK",
                                      "message": "처방전 승인 성공",
                                      "data": {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "timeSlotId": "timeslot123",
                                        "userId": "user123",
                                        "petId": "pet123",
                                        "petName": "멍멍이",
                                        "imageUrls": [
                                          "https://s3.amazonaws.com/bucket/prescription1.jpg"
                                        ],
                                        "description": "감기약 처방전",
                                        "status": "APPROVED",
                                        "price": 50000,
                                        "createdAt": "2025-12-16T10:30:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 승인된 처방전",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX002",
                                      "errorMessage": "이미 승인된 처방전입니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "처방전을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX001",
                                      "errorMessage": "처방전을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> approvePrescription(
            @Parameter(description = "처방전 ID", example = "550e8400-e29b-41d4-a716-446655440000") String prescriptionId,
            PrescriptionApproveRequest request,
            MultipartFile image);

    @Operation(
            summary = "처방전 거절",
            description = """
                    처방전을 거절합니다.

                    **거절 조건**
                    - 처방전 상태가 PENDING이어야 함
                    - 이미 승인되거나 거절된 처방전은 거절 불가

                    **처리 흐름**
                    1. 처방전 상태를 REJECTED로 변경

                    **사용 화면**: 관리자 > 처방전 관리 > 거절
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "처방전 거절 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD041",
                                      "httpStatus": "OK",
                                      "message": "처방전 거절 성공",
                                      "data": {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "timeSlotId": "timeslot123",
                                        "userId": "user123",
                                        "petId": "pet123",
                                        "petName": "멍멍이",
                                        "imageUrls": [
                                          "https://s3.amazonaws.com/bucket/prescription1.jpg"
                                        ],
                                        "description": "감기약 처방전",
                                        "status": "REJECTED",
                                        "price": null,
                                        "createdAt": "2025-12-16T10:30:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 거절된 처방전",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX003",
                                      "errorMessage": "이미 거절된 처방전입니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "처방전을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX001",
                                      "errorMessage": "처방전을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> rejectPrescription(
            @Parameter(description = "처방전 ID", example = "550e8400-e29b-41d4-a716-446655440000") String prescriptionId);

    @Operation(
            summary = "처방전 상태 복원 (PENDING으로)",
            description = """
                    처방전 상태를 PENDING으로 복원합니다.

                    **처리 흐름**
                    1. 처방전 상태를 PENDING으로 변경
                    2. 설정된 가격 초기화

                    **사용 화면**: 관리자 > 처방전 관리 > 상태 복원
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "처방전 상태 복원 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse> revertPrescription(
            @Parameter(description = "처방전 ID", example = "550e8400-e29b-41d4-a716-446655440000") String prescriptionId);

}
