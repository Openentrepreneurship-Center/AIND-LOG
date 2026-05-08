package com.backend.domain.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.schedule.dto.response.ScheduleResponse;
import com.backend.domain.schedule.service.ScheduleService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController implements ScheduleApi {

    private final ScheduleService scheduleService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getAvailableSchedules(
            @RequestParam int year,
            @RequestParam int month) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByMonth(year, month);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SCHEDULE_LIST_SUCCESS, schedules));
    }

    @Override
    @GetMapping("/{scheduleId}")
    public ResponseEntity<SuccessResponse> getSchedule(@PathVariable String scheduleId) {
        ScheduleResponse response = scheduleService.getSchedule(scheduleId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SCHEDULE_GET_SUCCESS, response));
    }
}
