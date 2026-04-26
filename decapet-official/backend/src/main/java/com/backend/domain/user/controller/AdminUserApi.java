package com.backend.domain.user.controller;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.backend.domain.user.dto.request.UpdateBuyerGradeRequest;
import com.backend.domain.user.dto.request.UpdatePermissionsRequest;
import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[Admin] 사용자", description = "사용자 관리")
public interface AdminUserApi {

    @Operation(
            summary = "회원 목록 조회",
            description = """
                    전체 회원 목록을 페이징하여 조회합니다.

                    **페이징 파라미터**
                    - page: 페이지 번호 (0부터 시작)
                    - size: 페이지 크기 (기본값: 20)
                    - sort: 정렬 기준 (예: createdAt,desc)

                    **응답 정보**
                    - 회원 기본 정보 (이름, 이메일, 전화번호, 주소)
                    - 구매자 등급 및 권한 목록
                    - 관리자 메모, 관리자 메모 2
                    - 반려동물 목록
                    - 가입일 및 수정일

                    **사용 화면**: 관리자 > 회원 관리
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
                                      "code": "U004",
                                      "httpStatus": "OK",
                                      "message": "회원 목록 조회 성공",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "01HQXK5V7B2M3N4P5Q6R7S8T9U",
                                            "uniqueNumber": "U-20240115-ABC123",
                                            "name": "홍길동",
                                            "email": "hong@example.com",
                                            "phone": "01012345678",
                                            "zipCode": "06234",
                                            "address": "서울특별시 강남구 테헤란로 123",
                                            "buyerGrade": "1",
                                            "adminMemo": "메모",
                                            "adminMemo2": "추가 메모",
                                            "permissions": ["PRODUCT_PURCHASE", "APPOINTMENT_BOOKING"],
                                            "createdAt": "2024-01-15T10:30:00",
                                            "updatedAt": "2024-03-20T14:20:00",
                                            "pets": [
                                              {
                                                "id": "01HQXK5V7B2M3N4P5Q6R7S8T9V",
                                                "uniqueNumber": "P-20240115-XYZ789",
                                                "name": "뽀삐",
                                                "breedName": "말티즈",
                                                "species": "DOG"
                                              }
                                            ]
                                          }
                                        ],
                                        "totalElements": 150,
                                        "totalPages": 8,
                                        "size": 20,
                                        "number": 0
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
    ResponseEntity<SuccessResponse> getUsers(
            @RequestParam(required = false) @Parameter(description = "검색어 (이름, 이메일, 전화번호, 고유번호)") String searchText,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "시작일 (yyyy-MM-dd)") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "종료일 (yyyy-MM-dd)") LocalDate endDate,
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "회원 관리 정보 수정",
            description = """
                    회원의 구매자 등급 및 관리자 메모를 수정합니다.

                    **수정 가능 항목**
                    - 구매자 등급 (1, 2, 3, 4 또는 null)
                    - 관리자 메모 (최대 1000자)
                    - 관리자 메모 2 (최대 1000자)

                    **참고 사항**
                    - 관리자 메모는 회원에게 노출되지 않습니다.

                    **사용 화면**: 관리자 > 회원 관리 > 정보 수정
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
                                      "code": "U006",
                                      "httpStatus": "OK",
                                      "message": "회원 관리 정보 수정 성공",
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
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U001",
                                      "errorMessage": "사용자를 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> updateUserAdminInfo(
            @Parameter(description = "회원 ID", example = "01HQXK5V7B2M3N4P5Q6R7S8T9U") String userId,
            UpdateBuyerGradeRequest request
    );

    @Operation(
            summary = "회원 권한 수정",
            description = """
                    회원의 권한을 수정합니다.

                    **권한 종류**
                    - `PRODUCT_PURCHASE`: 제품 구매
                    - `APPOINTMENT_BOOKING`: 진료 예약
                    - `INFORMATION_SHARING`: 정보 공유

                    **주의**: 기존 권한은 모두 삭제되고, 요청한 권한으로 교체됩니다.

                    **사용 화면**: 관리자 > 회원 관리 > 권한 수정
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "권한 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U007",
                                      "httpStatus": "OK",
                                      "message": "회원 권한 수정 성공",
                                      "data": null
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U001",
                                      "errorMessage": "사용자를 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> updatePermissions(
            @Parameter(description = "회원 ID", example = "01HQXK5V7B2M3N4P5Q6R7S8T9U") String userId,
            @Valid UpdatePermissionsRequest request
    );

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    관리자가 회원을 탈퇴시킵니다.

                    **주의**: 이 작업은 soft delete로 처리됩니다.

                    **사용 화면**: 관리자 > 회원 관리 > 회원 탈퇴
                    """
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "탈퇴 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "C001",
                                      "httpStatus": "OK",
                                      "message": "삭제 성공",
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
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U001",
                                      "errorMessage": "사용자를 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> deleteUser(
            @Parameter(description = "회원 ID", example = "01HQXK5V7B2M3N4P5Q6R7S8T9U") String userId
    );

}
