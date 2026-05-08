package com.backend.domain.schedule.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.schedule.dto.request.CreateScheduleRequest;
import com.backend.domain.schedule.dto.request.TimeSlotRequest;
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

@Tag(name = "[Admin] 스케줄", description = "스케줄 관리")
@SecurityRequirement(name = "cookieAuth")
public interface AdminScheduleApi {

	@Operation(
		summary = "일정 생성",
		description = """
			새로운 예약 일정을 생성합니다.
			
			**필수 정보**
			- 날짜: 일정 날짜 (LocalDate)
			- 시간대 목록: 최소 1개 이상의 시간대 필요
			
			**생성 규칙**
			- 동일한 날짜에 중복 일정 생성 불가
			- 과거 날짜에 일정 생성 불가
			- 각 시간대는 자동으로 예약 가능 상태로 생성됨
			
			**시간대 정보**
			- 각 시간대는 고유 ID를 부여받음
			- 초기 상태는 모두 available=true
			
			**사용 화면**: 관리자 > 스케줄 관리 > 생성
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "일정 생성 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SC001",
					  "httpStatus": "CREATED",
					  "message": "일정 생성 성공",
					  "data": {
					    "id": "schedule123",
					    "date": "2025-12-20",
					    "timeSlots": [
					      {
					        "id": "slot123",
					        "time": "10:00:00",
					        "location": "서울시 강남구 테헤란로 123",
					        "available": true
					      },
					      {
					        "id": "slot124",
					        "time": "14:00:00",
					        "location": "서울시 강남구 테헤란로 123",
					        "available": true
					      }
					    ]
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
			responseCode = "409",
			description = "중복된 일정",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "G003",
					  "errorMessage": "이미 존재하는 데이터입니다.",
					  "httpStatus": "CONFLICT"
					}
					""")
			)
		)
	})
	ResponseEntity<SuccessResponse> createSchedule(CreateScheduleRequest request);

	@Operation(
		summary = "일정 상세 조회",
		description = """
			일정의 상세 정보를 조회합니다.
			
			**응답 정보**
			- 일정 ID 및 날짜
			- 해당 일정의 모든 시간대 정보
			- 각 시간대의 시간, 장소, 예약 가능 여부
			
			**권한**: 관리자는 모든 일정 조회 가능
			
			**사용 화면**: 관리자 > 스케줄 관리 > 상세
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "일정 상세 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SC002",
					  "httpStatus": "OK",
					  "message": "일정 조회 성공",
					  "data": {
					    "id": "schedule123",
					    "date": "2025-12-20",
					    "timeSlots": [
					      {
					        "id": "slot123",
					        "time": "10:00:00",
					        "location": "서울시 강남구 테헤란로 123",
					        "available": true
					      },
					      {
					        "id": "slot124",
					        "time": "14:00:00",
					        "location": "서울시 강남구 테헤란로 123",
					        "available": false
					      }
					    ]
					  }
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "일정을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AP001",
					  "errorMessage": "예약 일정을 찾을 수 없습니다.",
					  "httpStatus": "NOT_FOUND"
					}
					""")
			)
		)
	})
	ResponseEntity<SuccessResponse> getSchedule(
		@Parameter(description = "일정 ID", example = "550e8400-e29b-41d4-a716-446655440000") String scheduleId);

	@Operation(
		summary = "일정 목록 조회",
		description = """
			월별 일정 목록을 조회합니다.
			
			**조회 조건**
			- year: 연도
			- month: 월 (1-12)
			
			**정렬**: 날짜 오름차순 정렬
			
			**사용 화면**: 관리자 > 스케줄 관리 > 목록
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "일정 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SC003",
					  "httpStatus": "OK",
					  "message": "일정 목록 조회 성공",
					  "data": [
					    {
					      "id": "schedule123",
					      "date": "2025-12-20",
					      "timeSlots": [
					        {
					          "id": "slot123",
					          "time": "10:00:00",
					          "location": "서울시 강남구 테헤란로 123",
					          "available": true
					        }
					      ]
					    },
					    {
					      "id": "schedule124",
					      "date": "2025-12-21",
					      "timeSlots": [
					        {
					          "id": "slot125",
					          "time": "11:00:00",
					          "location": "서울시 강남구 테헤란로 123",
					          "available": true
					        }
					      ]
					    }
					  ]
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 파라미터",
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
		)
	})
	ResponseEntity<SuccessResponse> getSchedules(
		@Parameter(description = "연도", example = "2025") int year,
		@Parameter(description = "월", example = "12") int month,
		@Parameter(description = "지난 일정 포함 여부", example = "false") boolean includePast);

	@Operation(
		summary = "일정 삭제",
		description = """
			일정을 삭제합니다.
			
			**삭제 조건**
			- 예약이 없는 일정만 삭제 가능
			- 예약이 있는 경우 삭제 불가
			
			**삭제 처리**
			- 일정 및 모든 시간대가 함께 삭제됨
			- 삭제된 일정은 복구 불가능
			
			**사용 화면**: 관리자 > 스케줄 관리 > 삭제
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "일정 삭제 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SC004",
					  "httpStatus": "OK",
					  "message": "일정 삭제 성공",
					  "data": null
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "일정을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AP001",
					  "errorMessage": "예약 일정을 찾을 수 없습니다.",
					  "httpStatus": "NOT_FOUND"
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "예약이 있는 일정 삭제 시도",
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
		)
	})
	ResponseEntity<SuccessResponse> deleteSchedule(
		@Parameter(description = "일정 ID", example = "550e8400-e29b-41d4-a716-446655440000") String scheduleId,
		@Parameter(description = "강제 삭제 여부") boolean force);

	@Operation(
		summary = "시간대 추가",
		description = """
			기존 일정에 새로운 시간대를 추가합니다.
			
			**필수 정보**
			- 시간 (LocalTime)
			- 장소 (String)
			
			**추가 규칙**
			- 동일 일정 내 중복 시간 불가
			- 새로운 시간대는 자동으로 예약 가능 상태로 생성됨
			
			**사용 화면**: 관리자 > 스케줄 관리 > 시간대 추가
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "시간대 추가 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SC005",
					  "httpStatus": "CREATED",
					  "message": "시간대 추가 성공",
					  "data": {
					    "id": "schedule123",
					    "date": "2025-12-20",
					    "timeSlots": [
					      {
					        "id": "slot123",
					        "time": "10:00:00",
					        "location": "서울시 강남구 테헤란로 123",
					        "available": true
					      },
					      {
					        "id": "slot126",
					        "time": "16:00:00",
					        "location": "서울시 강남구 테헤란로 123",
					        "available": true
					      }
					    ]
					  }
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "일정을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "AP001",
					  "errorMessage": "예약 일정을 찾을 수 없습니다.",
					  "httpStatus": "NOT_FOUND"
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "409",
			description = "중복된 시간대",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "G003",
					  "errorMessage": "이미 존재하는 데이터입니다.",
					  "httpStatus": "CONFLICT"
					}
					""")
			)
		)
	})
	ResponseEntity<SuccessResponse> addTimeSlot(
		@Parameter(description = "일정 ID", example = "550e8400-e29b-41d4-a716-446655440000") String scheduleId,
		TimeSlotRequest request);

	@Operation(
		summary = "시간대 삭제",
		description = """
			일정에서 특정 시간대를 삭제합니다.
			
			**삭제 조건**
			- 예약이 없는 시간대만 삭제 가능
			- 예약이 있는 시간대는 삭제 불가
			
			**삭제 처리**
			- 해당 시간대만 삭제됨
			- 일정에 시간대가 모두 삭제되면 일정도 삭제됨
			
			**사용 화면**: 관리자 > 스케줄 관리 > 시간대 삭제
			"""
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "시간대 삭제 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = SuccessResponse.class),
				examples = @ExampleObject(value = """
					{
					  "code": "SC006",
					  "httpStatus": "OK",
					  "message": "시간대 삭제 성공",
					  "data": null
					}
					""")
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "일정 또는 시간대를 찾을 수 없음",
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
			responseCode = "400",
			description = "예약이 있는 시간대 삭제 시도",
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
		)
	})
	ResponseEntity<SuccessResponse> deleteTimeSlot(
		@Parameter(description = "일정 ID", example = "550e8400-e29b-41d4-a716-446655440000") String scheduleId,
		@Parameter(description = "시간대 ID", example = "660e8400-e29b-41d4-a716-446655440000") String timeSlotId,
		@Parameter(description = "강제 삭제 여부") boolean force);
}
