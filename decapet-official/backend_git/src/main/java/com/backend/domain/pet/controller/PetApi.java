package com.backend.domain.pet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.pet.dto.request.PetRegisterRequest;
import com.backend.domain.pet.dto.request.PetUpdateRequest;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

import org.springframework.http.MediaType;
import jakarta.validation.Valid;

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

@Tag(name = "반려동물", description = "반려동물 등록 및 관리")
public interface PetApi {

    @Operation(
            summary = "반려동물 등록",
            description = """
                    새로운 반려동물을 등록합니다.

                    **요청 형식**: multipart/form-data
                    - data: JSON 문자열 (PetRegisterRequest)
                    - photo: 이미지 파일 (선택)

                    **필수 필드**
                    - name: 이름 (최대 50자)
                    - breedId: 품종 ID (/breeds API에서 조회)
                    - gender: MALE / FEMALE
                    - birthdate: 생년월일 (과거 날짜만 가능)
                    - weight: 체중 (0.1~100kg)

                    **선택 필드**
                    - neutered: 중성화 여부 (기본값: false)
                    - customBreed: 기타 품종명 (breedId가 '기타'인 경우)
                    - vets: 수의사 정보 배열

                    **처리 흐름**
                    1. 품종 ID 유효성 검증
                    2. 생년월일/체중 범위 검증
                    3. 사진 업로드 (S3)
                    4. 반려동물 정보 저장

                    **사용 화면**: 마이페이지 > 반려동물 등록
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P001",
                                      "httpStatus": "CREATED",
                                      "message": "반려동물 등록 성공",
                                      "data": {
                                        "petId": "507f1f77bcf86cd799439012"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검증 실패 또는 잘못된 데이터",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P004",
                                      "errorMessage": "체중은 0.1kg에서 100kg 사이여야 합니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증이 필요합니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "품종을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BR001",
                                      "errorMessage": "품종을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "고유번호 생성 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U011",
                                      "errorMessage": "고유번호 생성에 실패했습니다.",
                                      "httpStatus": "INTERNAL_SERVER_ERROR"
                                    }
                                    """)
                    )
            )
    })
    @RequestBody(
            description = "반려동물 등록 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> registerPet(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "반려동물 정보 (JSON)", required = true)
            @Valid PetRegisterRequest request,
            @Parameter(description = "반려동물 사진 (선택사항)")
            MultipartFile photo);

    @Operation(
            summary = "내 반려동물 목록",
            description = """
                    로그인한 사용자의 반려동물 목록을 조회합니다.

                    **응답 정보**
                    - 기본 정보: 이름, 품종, 성별, 나이
                    - 사진 URL
                    - 체중 및 중성화 여부

                    **정렬**: 등록일 최신순

                    **사용 화면**: 마이페이지 > 내 반려동물
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P005",
                                      "httpStatus": "OK",
                                      "message": "반려동물 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": "507f1f77bcf86cd799439012",
                                          "uniqueNumber": "P-20240115-XYZ789",
                                          "name": "뽀삐",
                                          "breedName": "포메라니안",
                                          "gender": "MALE",
                                          "age": 4,
                                          "photoUrl": "https://s3.amazonaws.com/bucket/pet-photo.jpg",
                                          "weight": 5.5,
                                          "neutered": true
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증이 필요합니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> getMyPets(@Parameter(hidden = true) String userId);

    @Operation(
            summary = "반려동물 상세 조회",
            description = """
                    특정 반려동물의 상세 정보를 조회합니다.

                    **응답 정보**
                    - 기본 정보: 이름, 품종, 성별, 생년월일, 나이
                    - 체중 정보: 현재 체중, 마지막 수정일
                    - 체중 수정 가능 여부 (canUpdateWeight)
                    - 다음 체중 수정 가능일 (nextWeightUpdateDate)
                    - 수의사 정보 목록
                    - 사진 URL

                    **체중 수정 규칙**
                    - 30일에 1회만 수정 가능
                    - canUpdateWeight: true일 때만 수정 가능

                    **사용 화면**: 반려동물 상세 페이지
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P002",
                                      "httpStatus": "OK",
                                      "message": "반려동물 정보 조회 성공",
                                      "data": {
                                        "id": "507f1f77bcf86cd799439012",
                                        "name": "뽀삐",
                                        "breedName": "포메라니안",
                                        "customBreed": null,
                                        "gender": "MALE",
                                        "neutered": true,
                                        "birthdate": "2020-03-15",
                                        "age": 4,
                                        "weight": 5.5,
                                        "weightUpdatedAt": "2024-03-01T10:00:00",
                                        "canUpdateWeight": false,
                                        "nextWeightUpdateDate": "2024-03-31",
                                        "photoUrl": "https://s3.amazonaws.com/bucket/pet-photo.jpg",
                                        "vets": [
                                          {
                                            "vetName": "서울동물병원",
                                            "vetPhone": "0212345678"
                                          }
                                        ],
                                        "createdAt": "2024-01-15T10:30:00",
                                        "updatedAt": "2024-03-01T10:00:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증이 필요합니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P005",
                                      "errorMessage": "해당 반려동물에 대한 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
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
    ResponseEntity<SuccessResponse> getPet(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "반려동물 ID", example = "507f1f77bcf86cd799439012") String petId
    );

    @Operation(
            summary = "반려동물 정보 수정",
            description = """
                    반려동물의 정보를 수정합니다.

                    **요청 형식**: multipart/form-data
                    - data: JSON 문자열 (수정할 필드만 포함)
                    - photo: 새 이미지 파일 (선택, 기존 이미지 대체)

                    **수정 가능 필드**
                    - name: 이름
                    - gender: 성별
                    - neutered: 중성화 여부
                    - weight: 체중 (30일 1회 제한)

                    **체중 수정 규칙**
                    - 30일에 1회만 수정 가능
                    - 상세 조회에서 canUpdateWeight 확인 필수
                    - weight 필드 생략 시 체중 미수정

                    **처리 방식**
                    - Partial Update: 제공된 필드만 수정
                    - photo 미전송 시 기존 사진 유지

                    **사용 화면**: 반려동물 정보 수정 페이지
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P003",
                                      "httpStatus": "OK",
                                      "message": "반려동물 정보 수정 성공",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "체중 수정 제한 또는 유효성 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P002",
                                      "errorMessage": "체중은 30일에 한 번만 수정할 수 있습니다.",
                                      "httpStatus": "BAD_REQUEST"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증이 필요합니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P005",
                                      "errorMessage": "해당 반려동물에 대한 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
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
            description = "반려동물 수정 요청",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    ResponseEntity<SuccessResponse> updatePet(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "반려동물 ID", example = "507f1f77bcf86cd799439012") String petId,
            @Parameter(description = "수정할 정보 (JSON)", required = true)
            @Valid PetUpdateRequest request,
            @Parameter(description = "새 반려동물 사진 (선택사항)")
            MultipartFile photo);

    @Operation(
            summary = "반려동물 삭제",
            description = """
                    반려동물 정보를 삭제합니다.

                    **삭제 제한 조건**
                    - 진행 중인 예약(PENDING/APPROVED)이 있는 경우
                    - 미완료 처방전이 있는 경우

                    **삭제 시 영향**
                    - 반려동물 정보 완전 삭제
                    - 관련 예약/처방전 이력 접근 불가
                    - 의약품 장바구니 삭제

                    **복구 불가**: 삭제된 데이터는 복구할 수 없습니다

                    **사용 화면**: 반려동물 상세 > 삭제 버튼
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P004",
                                      "httpStatus": "OK",
                                      "message": "반려동물 삭제 성공",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "A001",
                                      "errorMessage": "인증이 필요합니다.",
                                      "httpStatus": "UNAUTHORIZED"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "P005",
                                      "errorMessage": "해당 반려동물에 대한 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
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
    ResponseEntity<SuccessResponse> deletePet(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "반려동물 ID", example = "507f1f77bcf86cd799439012") String petId
    );
}
