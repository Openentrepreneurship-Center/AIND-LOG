package com.backend.domain.prescription.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.prescription.dto.request.CreatePrescriptionRequest;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

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

@Tag(name = "처방", description = "처방 관리")
@SecurityRequirement(name = "cookieAuth")
public interface PrescriptionApi {

    @Operation(
            summary = "처방전 요청",
            description = """
                    처방전 요청은 두 가지 타입으로 가능합니다.

                    **신청 타입**
                    1. SIMPLE (간단 신청)
                       - 텍스트 설명만으로 신청
                       - 첨부파일 없음
                       - description 필드 필수

                    2. DETAILED (상세 신청)
                       - 의약품 정보 + 첨부파일로 신청
                       - 첨부파일 1~4개 필수
                       - medicineName, medicineWeight, dosageFrequency, dosagePeriod 필드 필수

                    **비즈니스 로직**
                    - 요청 직후 PENDING (승인 대기) 상태로 생성
                    - 관리자 승인 시 APPROVED + 결제 금액 설정
                    - 관리자 거절 시 REJECTED로 변경

                    **요청 형식**: multipart/form-data
                    - attachments: 첨부파일들 (DETAILED 타입에서 1~4개 필수)
                    - data: JSON 데이터 (type, petId, ...)

                    **처리 흐름**
                    1. 신청 타입 선택 (SIMPLE/DETAILED)
                    2. 타입별 필수 필드 입력
                    3. 처방전 요청 생성
                    4. 관리자 승인 + 가격 설정
                    5. 사용자 결제

                    **사용 화면**: 처방전 요청 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "처방전 요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX001",
                                      "httpStatus": "CREATED",
                                      "message": "처방전 요청 성공",
                                      "data": {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "timeSlotId": "timeslot123",
                                        "userId": "user123",
                                        "petId": "pet123",
                                        "petName": "멍멍이",
                                        "type": "DETAILED",
                                        "description": null,
                                        "attachmentUrls": [
                                          "https://s3.amazonaws.com/bucket/prescriptions/uuid1.jpg",
                                          "https://s3.amazonaws.com/bucket/prescriptions/uuid2.jpg"
                                        ],
                                        "medicineName": "항생제 Amoxicillin",
                                        "medicineWeight": "500mg",
                                        "dosageFrequency": "하루 3회",
                                        "dosagePeriod": "7일",
                                        "additionalNotes": "음식과 함께 복용하지 마세요.",
                                        "status": "PENDING",
                                        "price": null,
                                        "createdAt": "2025-12-16T10:30:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패)",
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
                    responseCode = "404",
                    description = "반려동물을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P001",
                                      "errorMessage": "반려동물을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    @RequestBody(
            description = "처방전 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> createPrescription(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "첨부파일들 (DETAILED 타입에서 1~4개 필수, SIMPLE 타입에서는 불필요)", required = false) List<MultipartFile> attachments,
            @Parameter(description = "처방전 요청 데이터 (JSON)", required = true)
            @Valid CreatePrescriptionRequest request);

    @Operation(
            summary = "내 처방전 목록 조회",
            description = """
                    내가 요청한 처방전 목록을 조회합니다.

                    **비즈니스 로직**
                    - 본인의 모든 처방전 조회
                    - 기본 정렬: 최신순 (createdAt DESC)
                    - 페이지 크기 기본값: 20

                    **응답 정보**
                    - 처방전 ID, 타임슬롯 정보
                    - 반려동물 정보
                    - 처방전 이미지 URL 목록
                    - 처방전 상태 (PENDING, APPROVED, REJECTED)
                    - 결제 금액

                    **사용 화면**: 처방전 목록 페이지, 마이페이지
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
                                          },
                                          {
                                            "id": "660e8400-e29b-41d4-a716-446655440001",
                                            "timeSlotId": "timeslot124",
                                            "userId": "user123",
                                            "petId": "pet123",
                                            "petName": "멍멍이",
                                            "imageUrls": [
                                              "https://s3.amazonaws.com/bucket/prescription2.jpg"
                                            ],
                                            "description": "영양제 처방전",
                                            "status": "APPROVED",
                                            "price": 50000,
                                            "createdAt": "2025-12-15T14:20:00"
                                          }
                                        ],
                                        "pageable": {
                                          "pageNumber": 0,
                                          "pageSize": 20
                                        },
                                        "totalElements": 2,
                                        "totalPages": 1
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
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
            )
    })
    ResponseEntity<SuccessResponse> getMyPrescriptions(
            @Parameter(hidden = true) String userId,
            @ParameterObject Pageable pageable);

    @Operation(
            summary = "처방전 상세 조회",
            description = """
                    처방전 요청의 상세 정보를 조회합니다.

                    **비즈니스 로직**
                    - 본인의 처방전만 조회 가능

                    **응답 정보**
                    - 처방전 기본 정보 (ID, 상태, 생성일시)
                    - 타임슬롯 및 반려동물 정보
                    - 업로드된 처방전 이미지 URL 목록
                    - 설명 및 결제 금액

                    **사용 화면**: 처방전 상세 페이지
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
                                        "status": "APPROVED",
                                        "price": 50000,
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 사용자의 처방전)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX004",
                                      "errorMessage": "해당 처방전에 대한 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getPrescription(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "처방전 ID", example = "550e8400-e29b-41d4-a716-446655440000") String prescriptionId);

    @Operation(
            summary = "승인된 처방 조회",
            description = """
                    본인의 승인된(APPROVED) 처방 목록을 조회합니다.

                    **비즈니스 로직**
                    - 본인의 APPROVED 상태 처방만 조회

                    **사용 목적**
                    - /visit/products 페이지에서 "내 처방 의약품" 섹션 표시
                    - 승인된 처방에 대해 결제 버튼 제공

                    **사용 화면**: 의약품 목록 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "승인된 처방 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "RX003",
                                      "httpStatus": "OK",
                                      "message": "처방전 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": "550e8400-e29b-41d4-a716-446655440000",
                                          "petId": "pet123",
                                          "petName": "멍멍이",
                                          "type": "DETAILED",
                                          "medicineName": "넥스가드 스펙트라",
                                          "medicineWeight": "소형견용",
                                          "status": "APPROVED",
                                          "price": 50000,
                                          "createdAt": "2025-12-16T10:30:00"
                                        }
                                      ]
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getApprovedPrescriptions(
            @Parameter(hidden = true) String userId);
}
