package com.backend.domain.appointment.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.backend.domain.appointment.entity.AppointmentStatus;
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

@Tag(name = "[Admin] 예약", description = "예약 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminAppointmentApi {

	@Operation(
		summary = "예약 목록 조회",
		description = """
			모든 예약 목록을 조회합니다.

			**필터링**
			- status: 예약 상태 (PENDING, APPROVED, REJECTED, COMPLETED)
			- timeSlotId: 시간대 ID로 필터링 (timeSlotId 지정 시 해당 시간대의 예약 목록을 리스트로 반환)

			**정렬 및 페이징**
			- 기본 정렬: 최신순 (createdAt DESC)
			- 페이지 크기 기본값: 20
			- timeSlotId 필터 사용 시 페이징 없이 전체 리스트 반환

			**응답 정보**
			- 예약 ID, 사용자 및 반려동물 정보
			- 예약 날짜, 시간, 장소
			- 의약품 목록 및 총 금액
			- 예약 상태 및 생성 일시

			**사용 화면**: 관리자 > 예약 관리 > 목록
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
	ResponseEntity<SuccessResponse> getAppointments(
		@Parameter(description = "예약 상태 필터 (선택사항)", example = "PENDING") AppointmentStatus status,
		@Parameter(description = "시간대 ID 필터 (선택사항)", example = "slot123") String timeSlotId,
		@Parameter(description = "검색 키워드 - 이름/전화번호/반려동물 (선택사항)") String keyword,
		@ParameterObject Pageable pageable);

	@Operation(
		summary = "예약 상세 조회",
		description = """
			예약 상세 정보를 조회합니다.
			
			**응답 정보**
			- 예약 기본 정보 (날짜, 시간, 장소)
			- 사용자 및 반려동물 정보
			- 처방된 의약품 목록
			- 총 결제 금액
			- 예약 상태 및 생성 일시
			
			**권한**: 관리자는 모든 예약 조회 가능
			
			**사용 화면**: 관리자 > 예약 관리 > 상세
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
		)
	})
	ResponseEntity<SuccessResponse> getAppointment(
		@Parameter(description = "예약 ID", example = "550e8400-e29b-41d4-a716-446655440000") String appointmentId);

	@Operation(
		summary = "예약 승인",
		description = """
			예약을 승인합니다.
			
			**승인 조건**
			- 예약 상태가 PENDING이어야 함
			- 이미 승인되거나 거절된 예약은 승인 불가
			
			**처리 흐름**
			1. 예약 상태를 APPROVED로 변경
			2. 사용자가 처방전 등록 가능 상태가 됨
			3. 해당 시간대가 예약됨으로 표시됨
			
			**사용 화면**: 관리자 > 예약 관리 > 승인
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "예약 승인 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AD030",
					  "httpStatus": "OK",
					  "message": "예약 승인 성공",
					  "data": {
					    "id": "550e8400-e29b-41d4-a716-446655440000",
					    "petId": "pet123",
					    "petName": "멍멍이",
					    "appointmentDate": "2025-12-20",
					    "appointmentTime": "14:00:00",
					    "location": "서울시 강남구 테헤란로 123",
					    "medicines": [],
					    "totalAmount": 0,
					    "status": "APPROVED",
					    "createdAt": "2025-12-16T10:30:00"
					  }
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "이미 승인된 예약",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AP005",
					  "errorMessage": "이미 승인된 예약입니다.",
					  "httpStatus": "BAD_REQUEST"
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
		)
	})
	ResponseEntity<SuccessResponse> approveAppointment(
		@Parameter(description = "예약 ID", example = "550e8400-e29b-41d4-a716-446655440000") String appointmentId);

	@Operation(
		summary = "예약 거절",
		description = """
			예약을 거절합니다.
			
			**거절 조건**
			- 예약 상태가 PENDING이어야 함
			- 이미 승인되거나 거절된 예약은 거절 불가
			
			**처리 흐름**
			1. 예약 상태를 REJECTED로 변경
			2. 해당 시간대가 다시 예약 가능 상태가 됨
			3. 사용자에게 거절 알림 발송
			
			**사용 화면**: 관리자 > 예약 관리 > 거절
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "예약 거절 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AD031",
					  "httpStatus": "OK",
					  "message": "예약 거절 성공",
					  "data": {
					    "id": "550e8400-e29b-41d4-a716-446655440000",
					    "petId": "pet123",
					    "petName": "멍멍이",
					    "appointmentDate": "2025-12-20",
					    "appointmentTime": "14:00:00",
					    "location": "서울시 강남구 테헤란로 123",
					    "medicines": [],
					    "totalAmount": 0,
					    "status": "REJECTED",
					    "createdAt": "2025-12-16T10:30:00"
					  }
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "이미 거절된 예약",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AP006",
					  "errorMessage": "이미 거절된 예약입니다.",
					  "httpStatus": "BAD_REQUEST"
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
		)
	})
	ResponseEntity<SuccessResponse> rejectAppointment(
		@Parameter(description = "예약 ID", example = "550e8400-e29b-41d4-a716-446655440000") String appointmentId);
}
