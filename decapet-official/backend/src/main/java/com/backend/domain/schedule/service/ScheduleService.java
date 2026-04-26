package com.backend.domain.schedule.service;

import java.time.LocalDate;
import java.time.LocalTime;
import com.backend.global.util.DateTimeUtil;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.schedule.dto.mapper.TimeSlotResponseMapper;
import com.backend.domain.schedule.dto.response.ScheduleResponse;
import com.backend.domain.schedule.dto.response.TimeSlotResponse;
import com.backend.domain.schedule.entity.Schedule;
import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.schedule.exception.TimeSlotExpiredException;
import com.backend.domain.schedule.repository.ScheduleRepository;
import com.backend.domain.schedule.repository.TimeSlotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotResponseMapper timeSlotResponseMapper;

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        LocalDate today = DateTimeUtil.today();
        LocalTime now = DateTimeUtil.currentTime();

        // 과거 날짜는 제외
        if (startDate.isBefore(today)) {
            startDate = today;
        }

        List<Schedule> schedules = scheduleRepository.findByScheduleDateBetween(startDate, endDate);

        return schedules.stream()
                .map(schedule -> toResponseWithFutureSlots(schedule, today, now))
                .filter(response -> !response.timeSlots().isEmpty())
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getSchedule(String scheduleId) {
        Schedule schedule = scheduleRepository.findByIdOrThrow(scheduleId);

        LocalDate today = DateTimeUtil.today();
        LocalTime now = DateTimeUtil.currentTime();

        return toResponseWithFutureSlots(schedule, today, now);
    }

    public TimeSlot getTimeSlot(String timeSlotId) {
        return timeSlotRepository.findByIdOrThrow(timeSlotId);
    }

    /**
     * TimeSlot이 현재 시간 기준으로 유효한지 검증합니다.
     * 과거의 TimeSlot인 경우 TimeSlotExpiredException을 발생시킵니다.
     */
    public void validateTimeSlotNotExpired(TimeSlot timeSlot) {
        LocalDate today = DateTimeUtil.today();
        LocalTime now = DateTimeUtil.currentTime();

        LocalDate slotDate = timeSlot.getSchedule().getScheduleDate();
        LocalTime slotTime = timeSlot.getTime();

        boolean isExpired = slotDate.isBefore(today) ||
                           (slotDate.isEqual(today) && slotTime.isBefore(now));

        if (isExpired) {
            throw new TimeSlotExpiredException();
        }
    }

    private ScheduleResponse toResponseWithFutureSlots(Schedule schedule, LocalDate today, LocalTime now) {
        List<TimeSlotResponse> futureSlots = schedule.getTimeSlots().stream()
                .filter(slot -> isFutureTimeSlot(schedule.getScheduleDate(), slot.getTime(), today, now))
                .map(this::toTimeSlotResponse)
                .toList();

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getScheduleDate(),
                futureSlots
        );
    }

    private boolean isFutureTimeSlot(LocalDate scheduleDate, LocalTime slotTime, LocalDate today, LocalTime now) {
        if (scheduleDate.isAfter(today)) {
            return true;
        }
        if (scheduleDate.isEqual(today)) {
            return slotTime.isAfter(now);
        }
        return false;
    }

    private TimeSlotResponse toTimeSlotResponse(TimeSlot slot) {
        return timeSlotResponseMapper.toResponse(slot);
    }
}
