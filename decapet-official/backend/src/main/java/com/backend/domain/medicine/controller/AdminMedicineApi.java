package com.backend.domain.medicine.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.medicine.dto.request.CreateMedicineRequest;
import com.backend.domain.medicine.dto.request.UpdateMedicineRequest;
import com.backend.domain.medicine.entity.MedicineType;
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

@Tag(name = "[Admin] 의약품", description = "의약품 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminMedicineApi {

    @Operation(
        summary = "의약품 등록",
        description = """
            새 의약품을 등록합니다.

            **요청 형식**: multipart/form-data
            - request: 의약품 정보 (JSON)
            - image: 의약품 이미지 파일 (필수)

            **처리 흐름**
            1. 이미지 업로드 (S3)
            2. 의약품 정보 저장 (PostgreSQL)
            3. 의약품 ID 반환

            **제약사항**
            - 의약품명: 필수, 공백 불가
            - 가격: 필수, 0보다 커야 함
            - 타입: 필수 (PARASITE, SKIN, COGNITIVE, OTHER 중 선택)
            - 유통기한: 필수
            - 이미지: 필수
            - 설문지: 필수, 최소 1개 이상

            **선택 항목**
            - 체중 범위 (minWeight, maxWeight): 적용 가능한 반려동물 체중 범위

            **사용 화면**: 관리자 > 의약품 관리 > 등록
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "의약품 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M003",
                          "httpStatus": "CREATED",
                          "message": "의약품 등록 성공",
                          "data": {
                            "id": "507f1f77bcf86cd799439011",
                            "name": "넥스가드 스펙트라",
                            "description": "개에 대한 벼룩, 진드기 및 내부 기생충 예방 및 치료",
                            "price": 35000,
                            "type": "PARASITE",
                            "minWeight": 2.0,
                            "maxWeight": 60.0,
                            "expirationDate": "2025-12-31",
                            "imageUrl": "https://s3.amazonaws.com/bucket/medicine1.jpg",
                            "monthsPerUnit": 3,
                            "questionnaire": [
                              {
                                "questionId": "q1",
                                "question": "반려동물의 현재 건강 상태는 어떠신가요?"
                              }
                            ],
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
            description = "의약품 등록 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> createMedicine(
        @Valid
        @Parameter(description = "의약품 생성 요청 정보 (JSON)", required = true)
        CreateMedicineRequest request,

        @Parameter(description = "의약품 이미지 파일 (필수)", required = true)
        MultipartFile image
    );

    @Operation(
        summary = "의약품 상세 조회",
        description = """
            특정 의약품의 상세 정보를 조회합니다.

            **조회 정보**
            - 의약품 기본 정보 (이름, 설명, 가격, 이미지)
            - 타입 및 체중 범위
            - 설문지 정보
            - 생성일 및 수정일

            **권한**: 관리자는 활성/비활성 상태와 관계없이 모든 의약품 조회 가능

            **사용 화면**: 관리자 > 의약품 관리 > 상세
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "의약품 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M001",
                          "httpStatus": "OK",
                          "message": "의약품 조회 성공",
                          "data": {
                            "id": "507f1f77bcf86cd799439011",
                            "name": "넥스가드 스펙트라",
                            "description": "개에 대한 벼룩, 진드기 및 내부 기생충 예방 및 치료",
                            "price": 35000,
                            "type": "PARASITE",
                            "minWeight": 2.0,
                            "maxWeight": 60.0,
                            "expirationDate": "2025-12-31",
                            "imageUrl": "https://s3.amazonaws.com/bucket/medicine1.jpg",
                            "monthsPerUnit": 3,
                            "questionnaire": [
                              {
                                "questionId": "q1",
                                "question": "반려동물의 현재 건강 상태는 어떠신가요?"
                              }
                            ],
                            "createdAt": "2025-01-15T09:30:00"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "의약품을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M001",
                          "errorMessage": "의약품을 찾을 수 없습니다.",
                          "httpStatus": "NOT_FOUND"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<SuccessResponse> getMedicine(
        @Parameter(description = "조회할 의약품 ID", example = "507f1f77bcf86cd799439011", required = true)
        String medicineId
    );

    @Operation(
        summary = "의약품 목록 조회 (관리자용)",
        description = """
            전체 의약품 목록을 조회합니다.

            **조회 범위**
            - 활성/비활성 상태 모두 포함
            - 타입별 필터링 지원 (PARASITE, SKIN, COGNITIVE, OTHER)

            **페이지네이션**
            - page: 페이지 번호 (0부터 시작, 기본값: 0)
            - size: 페이지 크기 (기본값: 20)
            - sort: 정렬 기준 (예: createdAt,desc, name,asc, price,asc)

            **사용 화면**: 관리자 > 의약품 관리 > 목록
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "의약품 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M002",
                          "httpStatus": "OK",
                          "message": "의약품 목록 조회 성공",
                          "data": {
                            "medicines": [
                              {
                                "id": "507f1f77bcf86cd799439011",
                                "name": "넥스가드 스펙트라",
                                "description": "개에 대한 벼룩, 진드기 및 내부 기생충 예방 및 치료",
                                "price": 35000,
                                "type": "PARASITE",
                                "minWeight": 2.0,
                                "maxWeight": 60.0,
                                "expirationDate": "2025-12-31",
                                "imageUrl": "https://s3.amazonaws.com/bucket/medicine1.jpg",
                                "monthsPerUnit": 3,
                                "questionnaire": [
                                  {
                                    "questionId": "q1",
                                    "question": "반려동물의 현재 건강 상태는 어떠신가요?"
                                  }
                                ],
                                "createdAt": "2025-01-15T09:30:00"
                              }
                            ],
                            "page": 0,
                            "size": 20,
                            "totalElements": 50,
                            "totalPages": 3,
                            "hasNext": true,
                            "hasPrevious": false
                          }
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<SuccessResponse> getAllMedicines(
        @Parameter(
            description = "의약품 타입 필터 (선택사항)",
            example = "PARASITE",
            schema = @Schema(implementation = MedicineType.class)
        )
        MedicineType type,
        @Parameter(
            description = "의약품명 검색 키워드 (선택사항)",
            example = "넥스가드"
        )
        String keyword,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "의약품 수정",
        description = """
            의약품 정보를 수정합니다.

            **요청 형식**: multipart/form-data
            - request: 의약품 정보 (JSON)
            - image: 의약품 이미지 (선택, 제공 시 기존 이미지 교체)

            **수정 가능 항목**
            - 의약품명, 설명, 가격, 타입, 체중 범위, 유통기한, 설문지, 이미지

            **제약사항**
            - 의약품명: 필수, 공백 불가
            - 가격: 필수, 0보다 커야 함
            - 타입: 필수
            - 유통기한: 필수
            - 설문지: 필수, 최소 1개 이상

            **처리 흐름**
            1. 의약품 조회
            2. 이미지 제공 시 S3 업로드
            3. 의약품 정보 업데이트

            **사용 화면**: 관리자 > 의약품 관리 > 수정
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "의약품 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M004",
                          "httpStatus": "OK",
                          "message": "의약품 수정 성공",
                          "data": {
                            "id": "507f1f77bcf86cd799439011",
                            "name": "넥스가드 스펙트라 (수정됨)",
                            "description": "업데이트된 설명입니다.",
                            "price": 38000,
                            "type": "PARASITE",
                            "minWeight": 2.5,
                            "maxWeight": 65.0,
                            "expirationDate": "2026-12-31",
                            "imageUrl": "https://s3.amazonaws.com/bucket/medicine1-updated.jpg",
                            "monthsPerUnit": 3,
                            "questionnaire": [
                              {
                                "questionId": "q1",
                                "question": "수정된 질문입니다."
                              }
                            ],
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
            description = "의약품을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M001",
                          "errorMessage": "의약품을 찾을 수 없습니다.",
                          "httpStatus": "NOT_FOUND"
                        }
                        """
                )
            )
        )
    })
    @RequestBody(
            description = "의약품 수정 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> updateMedicine(
        @Parameter(description = "수정할 의약품 ID", example = "507f1f77bcf86cd799439011", required = true)
        String medicineId,

        @Valid
        @Parameter(description = "의약품 수정 요청 정보 (JSON)", required = true)
        UpdateMedicineRequest request,

        @Parameter(description = "새 의약품 이미지 파일 (선택사항, 제공 시 기존 이미지 교체)", required = false)
        MultipartFile image
    );

    @Operation(
        summary = "의약품 삭제",
        description = """
            의약품을 삭제합니다 (소프트 삭제).

            **처리 방식**
            - 실제 데이터는 삭제되지 않음
            - 활성 상태만 비활성화로 변경
            - 이미 처방된 의약품의 이력은 유지

            **삭제 효과**
            - 의약품 목록에서 제외됨
            - 구매 및 처방 불가능
            - 관리자 페이지에서는 조회 가능

            **사용 화면**: 관리자 > 의약품 관리 > 삭제
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "의약품 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M005",
                          "httpStatus": "OK",
                          "message": "의약품 삭제 성공",
                          "data": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "의약품을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M001",
                          "errorMessage": "의약품을 찾을 수 없습니다.",
                          "httpStatus": "NOT_FOUND"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<SuccessResponse> deleteMedicine(
        @Parameter(description = "삭제할 의약품 ID", example = "507f1f77bcf86cd799439011", required = true)
        String medicineId
    );
}
