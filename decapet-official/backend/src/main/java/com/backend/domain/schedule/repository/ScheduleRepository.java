package com.backend.domain.schedule.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.schedule.entity.Schedule;
import com.backend.domain.schedule.exception.ScheduleNotFoundException;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {

    @EntityGraph(attributePaths = {"timeSlots"})
    List<Schedule> findByScheduleDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByScheduleDate(LocalDate scheduleDate);

    default Schedule findByIdOrThrow(String id) {
        return findById(id).orElseThrow(ScheduleNotFoundException::new);
    }
}
