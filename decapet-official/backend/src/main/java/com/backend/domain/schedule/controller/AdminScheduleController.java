package com.backend.domain.schedule.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.schedule.dto.request.CreateScheduleRequest;
import com.backend.domain.schedule.dto.request.TimeSlotRequest;
import com.backend.domain.schedule.dto.response.ScheduleResponse;
import com.backend.domain.schedule.dto.response.TimeSlotResponse;
import com.backend.domain.schedule.service.AdminScheduleService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleController implements AdminScheduleApi {

    private final AdminScheduleService adminScheduleService;

    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse> createSchedule(@RequestBody @Valid CreateScheduleRequest request) {
        ScheduleResponse response = adminScheduleService.createSchedule(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SCHEDULE_CREATE_SUCCESS, response));
    }

    @Override
    @GetMapping("/{scheduleId}")
    public ResponseEntity<SuccessResponse> getSchedule(@PathVariable String scheduleId) {
        ScheduleResponse response = adminScheduleService.getSchedule(scheduleId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SCHEDULE_GET_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getSchedules(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "false") boolean includePast) {
        List<ScheduleResponse> response = adminScheduleService.getSchedulesByMonth(year, month, includePast);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SCHEDULE_LIST_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<SuccessResponse> deleteSchedule(
            @PathVariable String scheduleId,
            @RequestParam(defaultValue = "false") boolean force) {
        adminScheduleService.deleteSchedule(scheduleId, force);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SCHEDULE_DELETE_SUCCESS));
    }

    @Override
    @PostMapping("/{scheduleId}/time-slots")
    public ResponseEntity<SuccessResponse> addTimeSlot(
            @PathVariable String scheduleId,
            @RequestBody @Valid TimeSlotRequest request) {
        TimeSlotResponse response = adminScheduleService.addTimeSlot(scheduleId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.TIME_SLOT_ADD_SUCCESS, response));
    }

    @PatchMapping("/time-slots/{timeSlotId}/capacity")
    public ResponseEntity<SuccessResponse> updateTimeSlotCapacity(
            @PathVariable String timeSlotId,
            @RequestParam int maxCapacity) {
        TimeSlotResponse response = adminScheduleService.updateTimeSlotCapacity(timeSlotId, maxCapacity);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/{scheduleId}/time-slots/{timeSlotId}")
    public ResponseEntity<SuccessResponse> deleteTimeSlot(
            @PathVariable String scheduleId,
            @PathVariable String timeSlotId,
            @RequestParam(defaultValue = "false") boolean force) {
        adminScheduleService.deleteTimeSlot(scheduleId, timeSlotId, force);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.TIME_SLOT_DELETE_SUCCESS));
    }
}
