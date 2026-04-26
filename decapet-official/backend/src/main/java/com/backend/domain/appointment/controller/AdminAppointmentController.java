package com.backend.domain.appointment.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.appointment.dto.response.AppointmentResponse;
import com.backend.domain.appointment.entity.AppointmentStatus;
import com.backend.domain.appointment.service.AdminAppointmentService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController implements AdminAppointmentApi {

    private final AdminAppointmentService adminAppointmentService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) String timeSlotId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (timeSlotId != null) {
            List<AppointmentResponse> response = adminAppointmentService.getAppointmentsByTimeSlotId(timeSlotId);
            return ResponseEntity.ok(SuccessResponse.of(SuccessCode.APPOINTMENT_LIST_SUCCESS, response));
        }

        if (keyword != null && !keyword.isBlank()) {
            PageResponse<AppointmentResponse> response = adminAppointmentService.searchAppointments(status, keyword.trim(), pageable);
            return ResponseEntity.ok(SuccessResponse.of(SuccessCode.APPOINTMENT_LIST_SUCCESS, response));
        }

        PageResponse<AppointmentResponse> response = status != null
                ? adminAppointmentService.getAppointmentsByStatus(status, pageable)
                : adminAppointmentService.getAllAppointments(pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.APPOINTMENT_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{appointmentId}")
    public ResponseEntity<SuccessResponse> getAppointment(@PathVariable String appointmentId) {
        AppointmentResponse response = adminAppointmentService.getAppointment(appointmentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.APPOINTMENT_GET_SUCCESS, response));
    }

    @Override
    @PostMapping("/{appointmentId}/approve")
    public ResponseEntity<SuccessResponse> approveAppointment(@PathVariable String appointmentId) {
        AppointmentResponse response = adminAppointmentService.approveAppointment(appointmentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_APPOINTMENT_APPROVE_SUCCESS, response));
    }

    @Override
    @PostMapping("/{appointmentId}/reject")
    public ResponseEntity<SuccessResponse> rejectAppointment(@PathVariable String appointmentId) {
        AppointmentResponse response = adminAppointmentService.rejectAppointment(appointmentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_APPOINTMENT_REJECT_SUCCESS, response));
    }

    @PostMapping("/{appointmentId}/revert")
    public ResponseEntity<SuccessResponse> revertAppointment(@PathVariable String appointmentId) {
        AppointmentResponse response = adminAppointmentService.revertAppointment(appointmentId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_UPDATE_SUCCESS, response));
    }
}
