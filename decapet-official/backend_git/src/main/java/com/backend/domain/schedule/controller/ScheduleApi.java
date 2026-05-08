package com.backend.domain.schedule.controller;

import org.springframework.http.ResponseEntity;

import com.backend.global.common.SuccessResponse;
import com.backend.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "스케줄", description = "예약 가능 시간 조회")
public interface ScheduleApi {

    @Operation(
            summary = "예약 가능 일정 조회",
            description = """
                    월 단위로 예약 가능한 일정을 조회합니다.

                    **비즈니스 로직**
                    - 예약 가능 상태(available)인 시간대만 포함
                    - 과거 일정은 제외됨
                    - 날짜순으로 정렬

                    **응답 정보**
                    - 해당 월의 모든 예약 가능 일정
                    - 각 일정의 날짜 및 시간대 정보
                    - 각 시간대의 예약 가능 여부

                    **파라미터**
                    - year: 연도 (예: 2025)
                    - month: 월 (1-12)

                    **사용 화면**: 예약 신청 페이지 (캘린더)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 가능 일정 조회 성공",
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
                                            },
                                            {
                                              "id": "slot124",
                                              "time": "14:00:00",
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
                    description = "잘못된 요청 (유효하지 않은 년도 또는 월)",
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
    ResponseEntity<SuccessResponse> getAvailableSchedules(
            @Parameter(description = "조회할 연도", example = "2025") int year,
            @Parameter(description = "조회할 월 (1-12)", example = "12") int month);

    @Operation(
            summary = "일정 상세 조회",
            description = """
                    특정 일정의 상세 정보와 시간대를 조회합니다.

                    **비즈니스 로직**
                    - 예약 신청 전 특정 일정의 상세 정보 확인
                    - 예약 가능한 시간대 확인

                    **응답 정보**
                    - 일정 ID 및 날짜
                    - 해당 일정의 모든 시간대 정보
                    - 각 시간대의 시간, 장소, 예약 가능 여부

                    **사용 화면**: 예약 신청 페이지 (특정 날짜 선택 시)
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
                                          },
                                          {
                                            "id": "slot125",
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
            )
    })
    ResponseEntity<SuccessResponse> getSchedule(
            @Parameter(description = "일정 ID", example = "550e8400-e29b-41d4-a716-446655440000") String scheduleId);
}
