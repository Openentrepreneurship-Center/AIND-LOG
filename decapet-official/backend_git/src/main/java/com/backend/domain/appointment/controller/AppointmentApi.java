package com.backend.domain.appointment.controller;

import java.time.LocalDateTime;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.backend.domain.appointment.dto.request.CreateAppointmentRequest;
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

@Tag(name = "예약", description = "진료 예약")
@SecurityRequirement(name = "cookieAuth")
public interface AppointmentApi {

    @Operation(
            summary = "예약 신청",
            description = """
                    진료 예약을 신청합니다.

                    **비즈니스 로직**
                    - 신청 직후 PENDING (승인 대기) 상태로 생성
                    - 관리자가 승인하면 APPROVED로 변경
                    - 관리자가 거절하면 REJECTED로 변경

                    **필수 조건**
                    - APPOINTMENT_BOOKING 권한 필요
                    - 반려동물이 사용자에게 등록되어 있어야 함
                    - 예약하려는 시간대가 예약 가능 상태여야 함

                    **처리 흐름**
                    1. 예약 가능 시간대 선택
                    2. 예약 신청
                    3. 관리자 승인 대기
                    4. 승인 후 결제 진행 가능

                    **사용 화면**: 예약 신청 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "예약 신청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AP001",
                                      "httpStatus": "CREATED",
                                      "message": "예약 신청 성공",
                                      "data": {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "petId": "pet123",
                                        "petName": "멍멍이",
                                        "appointmentDate": "2025-12-20",
                                        "appointmentTime": "14:00:00",
                                        "location": "서울시 강남구 테헤란로 123",
                                        "medicines": [],
                                        "totalAmount": 0,
                                        "status": "PENDING",
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
                    responseCode = "403",
                    description = "진료 예약 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "U008",
                                      "errorMessage": "진료 예약 권한이 없습니다.",
                                      "httpStatus": "FORBIDDEN"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "예약 시간대를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AP007",
                                      "errorMessage": "예약 시간대를 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 예약된 시간대",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AP003",
                                      "errorMessage": "이미 해당 일정에 예약이 있습니다.",
                                      "httpStatus": "CONFLICT"
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<SuccessResponse> createAppointment(
            @Parameter(hidden = true) String userId,
            @Valid CreateAppointmentRequest request);

    @Operation(
            summary = "내 예약 목록 조회",
            description = """
                    내가 신청한 예약 목록을 조회합니다.

                    **비즈니스 로직**
                    - 본인의 모든 예약 조회
                    - 기본 정렬: 최신순 (createdAt DESC)
                    - 페이지 크기 기본값: 20

                    **응답 정보**
                    - 예약 ID, 반려동물 정보
                    - 예약 날짜 및 시간, 장소
                    - 예약 상태 (PENDING, APPROVED, REJECTED, COMPLETED)
                    - 의약품 목록 및 총 금액

                    **사용 화면**: 예약 목록 페이지, 마이페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AP005",
                                      "httpStatus": "OK",
                                      "message": "예약 목록 조회 성공",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "550e8400-e29b-41d4-a716-446655440000",
                                            "petId": "pet123",
                                            "petName": "멍멍이",
                                            "appointmentDate": "2025-12-20",
                                            "appointmentTime": "14:00:00",
                                            "location": "서울시 강남구 테헤란로 123",
                                            "medicines": [],
                                            "totalAmount": 0,
                                            "status": "PENDING",
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
    ResponseEntity<SuccessResponse> getMyAppointments(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "조회 시작일 (ISO 8601)", example = "2024-09-28T00:00:00") LocalDateTime fromDate,
            @ParameterObject Pageable pageable);

    @Operation(
            summary = "예약 상세 조회",
            description = """
                    예약 상세 정보를 조회합니다.

                    **비즈니스 로직**
                    - 본인의 예약만 조회 가능

                    **응답 정보**
                    - 예약 기본 정보 (날짜, 시간, 장소)
                    - 반려동물 정보
                    - 처방된 의약품 목록 (승인 후)
                    - 총 결제 금액
                    - 예약 상태 및 생성 일시

                    **사용 화면**: 예약 상세 페이지
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AP004",
                                      "httpStatus": "OK",
                                      "message": "예약 조회 성공",
                                      "data": {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "petId": "pet123",
                                        "petName": "멍멍이",
                                        "appointmentDate": "2025-12-20",
                                        "appointmentTime": "14:00:00",
                                        "location": "서울시 강남구 테헤란로 123",
                                        "medicines": [
                                          {
                                            "medicineId": "med123",
                                            "medicineName": "강아지 영양제",
                                            "quantity": 2,
                                            "price": 15000
                                          }
                                        ],
                                        "totalAmount": 30000,
                                        "status": "APPROVED",
                                        "createdAt": "2025-12-16T10:30:00"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "예약을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "AP002",
                                      "errorMessage": "예약을 찾을 수 없습니다.",
                                      "httpStatus": "NOT_FOUND"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 사용자의 예약)",
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
    ResponseEntity<SuccessResponse> getAppointment(
            @Parameter(hidden = true) String userId,
            @Parameter(description = "예약 ID", example = "550e8400-e29b-41d4-a716-446655440000") String appointmentId);
}
