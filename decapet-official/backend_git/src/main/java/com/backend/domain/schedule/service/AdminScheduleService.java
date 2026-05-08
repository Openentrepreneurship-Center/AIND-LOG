package com.backend.domain.schedule.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.appointment.entity.AppointmentStatus;
import com.backend.domain.appointment.repository.AppointmentRepository;
import com.backend.domain.medicinecart.repository.MedicineCartRepository;
import com.backend.domain.payment.repository.PaymentRepository;
import com.backend.domain.schedule.dto.mapper.ScheduleMapper;
import com.backend.domain.schedule.dto.mapper.ScheduleResponseMapper;
import com.backend.domain.schedule.dto.mapper.TimeSlotMapper;
import com.backend.domain.schedule.dto.mapper.TimeSlotResponseMapper;
import com.backend.domain.schedule.dto.request.CreateScheduleRequest;
import com.backend.domain.schedule.dto.request.TimeSlotRequest;
import com.backend.domain.schedule.dto.response.ScheduleResponse;
import com.backend.domain.schedule.dto.response.TimeSlotResponse;
import com.backend.domain.schedule.entity.Schedule;
import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.schedule.exception.DuplicateScheduleException;
import com.backend.domain.schedule.exception.DuplicateTimeSlotException;
import com.backend.domain.schedule.repository.ScheduleRepository;
import com.backend.domain.schedule.repository.TimeSlotRepository;
import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminScheduleService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.PENDING, AppointmentStatus.APPROVED);

    private final ScheduleRepository scheduleRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final MedicineCartRepository medicineCartRepository;
    private final ScheduleMapper scheduleMapper;
    private final ScheduleResponseMapper scheduleResponseMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final TimeSlotResponseMapper timeSlotResponseMapper;

    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        if (request.date().isBefore(DateTimeUtil.today())) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_DATE);
        }

        if (scheduleRepository.existsByScheduleDate(request.date())) {
            throw new DuplicateScheduleException();
        }

        Schedule schedule = scheduleMapper.toEntity(request);
        Schedule saved = scheduleRepository.save(schedule);
        return scheduleResponseMapper.toResponse(saved);
    }

    public ScheduleResponse getSchedule(String scheduleId) {
        Schedule schedule = scheduleRepository.findByIdOrThrow(scheduleId);
        return scheduleResponseMapper.toResponse(schedule);
    }

    public List<ScheduleResponse> getSchedulesByMonth(int year, int month, boolean includePast) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        if (!includePast) {
            LocalDate today = DateTimeUtil.today();
            if (today.getYear() == year && today.getMonthValue() == month) {
                startDate = today;
            } else if (today.isAfter(endDate)) {
                return List.of();
            }
        }

        List<Schedule> schedules = scheduleRepository.findByScheduleDateBetween(startDate, endDate);
        return schedules.stream()
                .map(scheduleResponseMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteSchedule(String scheduleId, boolean force) {
        Schedule schedule = scheduleRepository.findByIdOrThrow(scheduleId);

        boolean hasActiveAppointments = schedule.getTimeSlots().stream()
                .anyMatch(slot -> appointmentRepository.existsByTimeSlotIdAndStatusIn(
                        slot.getId(), ACTIVE_STATUSES));
        if (hasActiveAppointments && !force) {
            throw new BusinessException(ErrorCode.TIMESLOT_HAS_APPOINTMENTS);
        }

        // 연관 데이터 정리
        for (TimeSlot slot : schedule.getTimeSlots()) {
            cleanupTimeSlotRelations(slot.getId());
        }

        scheduleRepository.delete(schedule);
    }

    @Transactional
    public TimeSlotResponse addTimeSlot(String scheduleId, TimeSlotRequest request) {
        Schedule schedule = scheduleRepository.findByIdOrThrow(scheduleId);

        boolean isDuplicate = schedule.getTimeSlots().stream()
                .anyMatch(slot -> slot.getTime().equals(request.time()));

        if (isDuplicate) {
            throw new DuplicateTimeSlotException();
        }

        TimeSlot timeSlot = timeSlotMapper.toEntity(request);
        schedule.addTimeSlot(timeSlot);

        return timeSlotResponseMapper.toResponse(timeSlot);
    }

    @Transactional
    public TimeSlotResponse updateTimeSlotCapacity(String timeSlotId, int maxCapacity) {
        TimeSlot timeSlot = timeSlotRepository.findByIdOrThrow(timeSlotId);
        timeSlot.updateMaxCapacity(maxCapacity);
        return timeSlotResponseMapper.toResponse(timeSlot);
    }

    @Transactional
    public void deleteTimeSlot(String scheduleId, String timeSlotId, boolean force) {
        Schedule schedule = scheduleRepository.findByIdOrThrow(scheduleId);
        TimeSlot timeSlot = timeSlotRepository.findByIdOrThrow(timeSlotId);

        if (appointmentRepository.existsByTimeSlotIdAndStatusIn(timeSlotId, ACTIVE_STATUSES) && !force) {
            throw new BusinessException(ErrorCode.TIMESLOT_HAS_APPOINTMENTS);
        }

        cleanupTimeSlotRelations(timeSlotId);
        schedule.removeTimeSlot(timeSlot);
    }

    private void cleanupTimeSlotRelations(String timeSlotId) {
        List<Appointment> appointments = appointmentRepository.findByTimeSlotId(timeSlotId);
        if (!appointments.isEmpty()) {
            List<String> appointmentIds = appointments.stream().map(Appointment::getId).toList();
            // 1. payment_appointments 조인 테이블 정리
            paymentRepository.deletePaymentAppointmentsByAppointmentIds(appointmentIds);
            // 2. Appointment soft-delete + timeSlot FK 해제
            for (Appointment appointment : appointments) {
                appointment.detachTimeSlot();
                appointment.delete();
            }
            appointmentRepository.flush();
        }
        // 3. MedicineCart 참조 해제
        medicineCartRepository.clearTimeSlotId(timeSlotId);
    }
}
