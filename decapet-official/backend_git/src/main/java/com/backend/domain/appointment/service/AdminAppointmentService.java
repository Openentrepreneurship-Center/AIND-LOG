package com.backend.domain.appointment.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.global.common.PageResponse;

import com.backend.domain.appointment.dto.mapper.AppointmentResponseMapper;
import com.backend.domain.appointment.dto.response.AppointmentResponse;
import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.appointment.entity.AppointmentStatus;
import com.backend.domain.appointment.repository.AppointmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentResponseMapper appointmentResponseMapper;

    public PageResponse<AppointmentResponse> getAllAppointments(Pageable pageable) {
        return PageResponse.from(
            appointmentRepository.findAll(pageable)
                .map(appointmentResponseMapper::toResponse)
        );
    }

    public PageResponse<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status, Pageable pageable) {
        return PageResponse.from(
            appointmentRepository.findByStatus(status, pageable)
                .map(appointmentResponseMapper::toResponse)
        );
    }

    public List<AppointmentResponse> getAppointmentsByTimeSlotId(String timeSlotId) {
        return appointmentRepository.findByTimeSlotId(timeSlotId).stream()
                .map(appointmentResponseMapper::toResponse)
                .toList();
    }

    public PageResponse<AppointmentResponse> searchAppointments(AppointmentStatus status, String keyword, Pageable pageable) {
        return PageResponse.from(
            appointmentRepository.searchByKeyword(status, keyword, pageable)
                .map(appointmentResponseMapper::toResponse)
        );
    }

    public AppointmentResponse getAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.getById(appointmentId);
        return appointmentResponseMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse approveAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.getById(appointmentId);
        appointment.approve();
        return appointmentResponseMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse rejectAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.getById(appointmentId);
        appointment.reject();
        return appointmentResponseMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse revertAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.getById(appointmentId);
        appointment.revertToPending();
        return appointmentResponseMapper.toResponse(appointment);
    }
}
