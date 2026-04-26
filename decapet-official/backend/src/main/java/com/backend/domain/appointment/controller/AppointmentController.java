package com.backend.domain.appointment.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.appointment.dto.request.CreateAppointmentRequest;
import com.backend.domain.appointment.dto.response.AppointmentResponse;
import com.backend.domain.appointment.service.AppointmentService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController implements AppointmentApi {

    private final AppointmentService appointmentService;

    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse> createAppointment(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.APPOINTMENT_REQUEST_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getMyAppointments(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<AppointmentResponse> response = appointmentService.getUserAppointments(userId, fromDate, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.APPOINTMENT_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{appointmentId}")
    public ResponseEntity<SuccessResponse> getAppointment(
            @AuthenticationPrincipal String userId,
            @PathVariable String appointmentId) {
        AppointmentResponse response = appointmentService.getAppointment(userId, appointmentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.APPOINTMENT_GET_SUCCESS, response));
    }
}
