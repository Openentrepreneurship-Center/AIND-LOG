package com.backend.domain.medicine.controller;

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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "의약품", description = "의약품 조회")
public interface MedicineApi {

    @Operation(
        summary = "메인 페이지 추천 의약품 조회",
        description = """
            메인 페이지에 표시할 추천 의약품을 조회합니다.

            **비즈니스 로직**
            - 활성화된 의약품 중 랜덤으로 4개를 선택하여 반환
            - 인증이 필요하지 않은 공개 API

            **응답 정보**
            - 의약품 ID, 이름, 가격, 유통기한, 이미지 URL

            **사용 화면**: 메인 페이지
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "추천 의약품 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "M001",
                          "httpStatus": "OK",
                          "message": "의약품 조회 성공",
                          "data": [
                            {
                              "id": "507f1f77bcf86cd799439011",
                              "name": "넥스가드 스펙트라",
                              "price": 35000,
                              "expirationDate": "2025-12-31",
                              "imageUrl": "https://example.com/medicine1.jpg"
                            },
                            {
                              "id": "507f1f77bcf86cd799439012",
                              "name": "애피킬 피부 치료제",
                              "price": 28000,
                              "expirationDate": "2026-06-30",
                              "imageUrl": "https://example.com/medicine2.jpg"
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
    ResponseEntity<SuccessResponse> getFeaturedMedicines();

    @Operation(
        summary = "의약품 목록 조회",
        description = """
            활성화된 의약품 목록을 조회합니다.

            **비즈니스 로직**
            - 판매 가능한 의약품들을 페이지네이션하여 반환
            - 인증 필수 API
            - 정렬 및 페이징 옵션 지원

            **반려동물 체중 필터링**
            - petId 파라미터 전달 시 해당 반려동물의 체중에 적합한 의약품만 반환
            - 본인 소유의 반려동물만 조회 가능
            - 의약품의 minWeight <= 반려동물체중 <= maxWeight 조건을 만족하는 항목만 필터링
            - minWeight 또는 maxWeight가 null인 의약품은 해당 조건 무시 (범위 제한 없음)
            - petId 미전달 시 전체 의약품 반환

            **페이지네이션 파라미터**
            - page: 페이지 번호 (0부터 시작, 기본값: 0)
            - size: 페이지 크기 (기본값: 10)
            - sort: 정렬 기준 (예: createdAt,desc, price,asc, name,asc)

            **사용 화면**: 의약품 목록 페이지
            """
    )
    @SecurityRequirement(name = "cookieAuth")
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
                                "imageUrl": "https://example.com/medicine1.jpg",
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
                            "size": 10,
                            "totalElements": 25,
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
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
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
            description = "반려동물 권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "P005",
                          "errorMessage": "해당 반려동물에 대한 권한이 없습니다.",
                          "httpStatus": "FORBIDDEN"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "반려동물을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "code": "P001",
                          "errorMessage": "반려동물을 찾을 수 없습니다.",
                          "httpStatus": "NOT_FOUND"
                        }
                        """
                )
            )
        )
    })
    ResponseEntity<SuccessResponse> getMedicines(
        @Parameter(hidden = true) String userId,
        @Parameter(description = "반려동물 ID - 해당 반려동물의 체중에 적합한 의약품만 필터링", example = "01HXK...")
        String petId,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "의약품 상세 조회",
        description = """
            특정 의약품의 상세 정보를 조회합니다.

            **비즈니스 로직**
            - 의약품의 기본 정보와 문진표를 반환
            - 인증이 필요하지 않은 공개 API

            **응답 정보**
            - 의약품 기본 정보 (ID, 이름, 설명, 가격, 타입, 유통기한, 이미지 URL)
            - 처방 문진표 목록 (질문 ID, 질문 내용)

            **사용 화면**: 의약품 상세 페이지
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
                            "expirationDate": "2025-12-31",
                            "imageUrl": "https://example.com/medicine1.jpg",
                            "monthsPerUnit": 3,
                            "questionnaire": [
                              {
                                "questionId": "q1",
                                "question": "반려동물의 현재 건강 상태는 어떠신가요?"
                              },
                              {
                                "questionId": "q2",
                                "question": "현재 복용 중인 약물이 있나요?"
                              }
                            ]
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
}
