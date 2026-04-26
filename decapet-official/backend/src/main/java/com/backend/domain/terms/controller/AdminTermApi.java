package com.backend.domain.terms.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.terms.dto.request.CreateTermRequest;
import com.backend.domain.terms.entity.TermType;
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

@Tag(name = "[Admin] 약관", description = "약관 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminTermApi {

    @Operation(
            summary = "약관 생성",
            description = """
                    새로운 약관을 생성합니다.

                    **약관 유형**
                    - SERVICE: 서비스 이용약관
                    - PRIVACY: 개인정보 처리방침

                    **처리 흐름**
                    1. 기존 최신 버전 확인
                    2. 버전 자동 증가
                    3. 약관 정보 저장

                    **제약사항**
                    - 시행일은 기존 최신 버전 이후여야 함
                    - 동일 유형/버전 중복 불가

                    **사용 화면**: 관리자 > 약관 관리 > 등록
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "약관 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD100",
                                      "httpStatus": "CREATED",
                                      "message": "생성 성공",
                                      "data": {
                                        "id": "term789",
                                        "type": "PRIVACY",
                                        "version": 2,
                                        "content": "개인정보 처리방침 v2 내용...",
                                        "isRequired": true,
                                        "effectiveDate": "2025-06-01"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 시행일 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T003",
                                      "errorMessage": "시행일은 기존 최신 버전의 시행일 이후여야 합니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복된 약관 버전",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T002",
                                      "errorMessage": "이미 존재하는 약관 버전입니다.",
                                      "httpStatus": "CONFLICT"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse> createTerm(@Valid CreateTermRequest request);

    @Operation(
            summary = "약관 목록 조회",
            description = """
                    모든 약관 목록을 조회합니다.

                    **필터링**
                    - SERVICE: 서비스 이용약관만
                    - PRIVACY: 개인정보 처리방침만
                    - 미지정: 전체 조회

                    **조회 범위**
                    - 모든 버전 포함
                    - 최신 버전순 정렬

                    **사용 화면**: 관리자 > 약관 관리 > 목록
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "약관 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T001",
                                      "httpStatus": "OK",
                                      "message": "약관 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": "term789",
                                          "type": "PRIVACY",
                                          "version": 2,
                                          "content": "개인정보 처리방침 v2 내용...",
                                          "isRequired": true,
                                          "effectiveDate": "2025-06-01"
                                        },
                                        {
                                          "id": "term456",
                                          "type": "PRIVACY",
                                          "version": 1,
                                          "content": "개인정보 처리방침 v1 내용...",
                                          "isRequired": true,
                                          "effectiveDate": "2025-01-01"
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getAllTerms(
            @Parameter(description = "약관 유형 필터 (SERVICE: 서비스 이용약관, PRIVACY: 개인정보 처리방침)", example = "SERVICE")
            TermType type,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            int page,
            @Parameter(description = "페이지 크기", example = "20")
            int size
    );

    @Operation(
            summary = "약관 상세 조회",
            description = """
                    특정 약관의 상세 정보를 조회합니다.

                    **응답 정보**
                    - 약관 ID, 유형, 버전
                    - 약관 내용
                    - 필수 여부 (isRequired)
                    - 시행일 (effectiveDate)

                    **사용 화면**: 관리자 > 약관 관리 > 상세
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "약관 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T002",
                                      "httpStatus": "OK",
                                      "message": "약관 조회 성공",
                                      "data": {
                                        "id": "term123",
                                        "type": "SERVICE",
                                        "version": 1,
                                        "content": "서비스 이용약관 내용...",
                                        "isRequired": true,
                                        "effectiveDate": "2025-01-01"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "약관을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "T001",
                                      "errorMessage": "약관을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getTerm(
            @Parameter(description = "약관 ID", required = true, example = "term123")
            String termId
    );
}
