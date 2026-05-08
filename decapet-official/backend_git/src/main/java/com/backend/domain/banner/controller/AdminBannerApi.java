package com.backend.domain.banner.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.banner.dto.request.CreateBannerRequest;
import com.backend.domain.banner.dto.request.ReorderBannersRequest;
import com.backend.domain.banner.dto.request.UpdateBannerRequest;
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

@Tag(name = "[Admin] 배너", description = "배너 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminBannerApi {

    @Operation(
            summary = "배너 생성",
            description = """
                    새로운 배너를 생성합니다.

                    **요청 형식**: multipart/form-data
                    - image: 배너 이미지 파일 (필수)
                    - request: 배너 정보 (JSON)

                    **처리 흐름**
                    1. 이미지 업로드 (S3)
                    2. 배너 정보 저장 (PostgreSQL)
                    3. 배너 ID 반환

                    **설정 항목**
                    - startAt/endAt: 노출 기간
                    - displayOrder: 자동으로 마지막 순서로 추가 (순서 변경은 별도 API 사용)

                    **사용 화면**: 관리자 > 배너 관리 > 등록
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "배너 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD100",
                                      "httpStatus": "CREATED",
                                      "message": "생성 성공",
                                      "data": {
                                        "id": "banner123",
                                        "title": "신규 회원 가입 이벤트",
                                        "imageUrl": "https://example.com/banner1.jpg",
                                        "displayOrder": 1,
                                        "startAt": "2025-12-01T00:00:00",
                                        "endAt": "2025-12-31T23:59:59"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 항목 누락, 유효하지 않은 날짜 범위 등)",
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
    @RequestBody(
            description = "배너 생성 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> createBanner(
            @Parameter(description = "배너 이미지 파일", required = true)
            MultipartFile image,
            @Parameter(description = "배너 정보 (JSON)", required = true)
            @Valid CreateBannerRequest request
    );

    @Operation(
            summary = "배너 목록 조회",
            description = """
                    모든 배너 목록을 조회합니다.

                    **조회 범위**
                    - 활성/비활성 배너 모두 포함
                    - 기간이 지난 배너 포함

                    **정렬 및 페이징**
                    - 기본 정렬: displayOrder ASC
                    - 페이지 크기 기본값: 10

                    **사용 화면**: 관리자 > 배너 관리 > 목록
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "배너 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BN001",
                                      "httpStatus": "OK",
                                      "message": "배너 목록 조회 성공",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "banner123",
                                            "title": "신규 회원 가입 이벤트",
                                            "imageUrl": "https://example.com/banner1.jpg",
                                            "displayOrder": 1,
                                            "startAt": "2025-12-01T00:00:00",
                                            "endAt": "2025-12-31T23:59:59"
                                          }
                                        ],
                                        "totalElements": 1,
                                        "totalPages": 1,
                                        "size": 10,
                                        "number": 0
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
    ResponseEntity<SuccessResponse> getAllBanners(
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "배너 상세 조회",
            description = """
                    특정 배너의 상세 정보를 조회합니다.

                    **응답 정보**
                    - 배너 ID, 제목
                    - 이미지 URL
                    - 노출 순서 (displayOrder)
                    - 노출 기간 (startAt ~ endAt)

                    **사용 화면**: 관리자 > 배너 관리 > 상세
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "배너 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BN001",
                                      "httpStatus": "OK",
                                      "message": "배너 목록 조회 성공",
                                      "data": {
                                        "id": "banner123",
                                        "title": "신규 회원 가입 이벤트",
                                        "imageUrl": "https://example.com/banner1.jpg",
                                        "displayOrder": 1,
                                        "startAt": "2025-12-01T00:00:00",
                                        "endAt": "2025-12-31T23:59:59"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "배너를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BN001",
                                      "errorMessage": "배너를 찾을 수 없습니다.",
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
    ResponseEntity<SuccessResponse> getBanner(
            @Parameter(description = "배너 ID", required = true, example = "banner123")
            String bannerId
    );

    @Operation(
            summary = "배너 수정",
            description = """
                    배너 정보를 수정합니다.

                    **요청 형식**: multipart/form-data
                    - image: 배너 이미지 파일 (선택, 제공시 변경)
                    - request: 배너 정보 (JSON)

                    **수정 가능 항목**
                    - 제목, 노출 기간
                    - 이미지 (선택사항)

                    **순서 변경**
                    - 표시 순서는 이 API에서 수정 불가
                    - 순서 변경은 PUT /reorder API를 사용

                    **이미지 처리**
                    - 새 이미지 제공 시: 기존 이미지 삭제 후 교체
                    - 미제공 시: 기존 이미지 유지

                    **사용 화면**: 관리자 > 배너 관리 > 수정
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "배너 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD101",
                                      "httpStatus": "OK",
                                      "message": "수정 성공",
                                      "data": {
                                        "id": "banner123",
                                        "title": "수정된 배너 제목",
                                        "imageUrl": "https://example.com/banner1-updated.jpg",
                                        "displayOrder": 2,
                                        "startAt": "2025-12-01T00:00:00",
                                        "endAt": "2025-12-31T23:59:59"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
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
                    description = "배너를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BN001",
                                      "errorMessage": "배너를 찾을 수 없습니다.",
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
    @RequestBody(
            description = "배너 수정 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> updateBanner(
            @Parameter(description = "배너 ID", required = true, example = "banner123")
            String bannerId,
            @Parameter(description = "배너 이미지 파일 (선택사항, 제공시 이미지 변경)")
            MultipartFile image,
            @Parameter(description = "배너 정보 (JSON)", required = true)
            @Valid UpdateBannerRequest request
    );

    @Operation(
            summary = "배너 삭제",
            description = """
                    배너를 삭제합니다.

                    **처리 흐름**
                    1. 배너 데이터 삭제
                    2. S3 이미지 파일 삭제

                    **주의사항**
                    - 삭제된 배너는 복구 불가
                    - 이미지 파일도 함께 삭제됨

                    **사용 화면**: 관리자 > 배너 관리 > 삭제
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "배너 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD102",
                                      "httpStatus": "OK",
                                      "message": "삭제 성공",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "배너를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BN001",
                                      "errorMessage": "배너를 찾을 수 없습니다.",
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
    ResponseEntity<SuccessResponse> deleteBanner(
            @Parameter(description = "배너 ID", required = true, example = "banner123")
            String bannerId
    );

    @Operation(
            summary = "배너 순서 변경",
            description = """
                    배너들의 표시 순서를 일괄 변경합니다.

                    **요청 형식**: application/json
                    - bannerIds: 변경할 배너 ID 목록 (원하는 순서대로)

                    **처리 흐름**
                    1. 요청된 배너 ID 목록으로 배너 조회
                    2. 배너 ID 목록 순서대로 displayOrder 재할당 (1부터 시작)
                    3. 변경된 배너 목록 반환

                    **주의사항**
                    - 요청 목록에 존재하지 않는 배너 ID가 포함되면 404 에러
                    - 전체 배너가 아닌 일부만 포함해도 해당 배너들의 순서만 변경됨

                    **사용 화면**: 관리자 > 배너 관리 > 순서 변경 (드래그앤드롭)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "배너 순서 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AD101",
                                      "httpStatus": "OK",
                                      "message": "수정 성공",
                                      "data": [
                                        {
                                          "id": "banner456",
                                          "title": "연말 특가 프로모션",
                                          "imageUrl": "https://example.com/banner2.jpg",
                                          "displayOrder": 1,
                                          "startAt": "2025-12-01T00:00:00",
                                          "endAt": "2025-12-31T23:59:59"
                                        },
                                        {
                                          "id": "banner123",
                                          "title": "신규 회원 가입 이벤트",
                                          "imageUrl": "https://example.com/banner1.jpg",
                                          "displayOrder": 2,
                                          "startAt": "2025-12-01T00:00:00",
                                          "endAt": "2025-12-31T23:59:59"
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (빈 목록 등)",
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
                    description = "배너를 찾을 수 없음 (목록에 존재하지 않는 ID 포함)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BN001",
                                      "errorMessage": "배너를 찾을 수 없습니다.",
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
    ResponseEntity<SuccessResponse> reorderBanners(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "배너 순서 변경 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReorderBannersRequest.class)
                    )
            )
            @Valid ReorderBannersRequest request
    );
}
