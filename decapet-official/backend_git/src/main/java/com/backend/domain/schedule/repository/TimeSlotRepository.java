package com.backend.domain.schedule.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.schedule.exception.TimeSlotNotFoundException;

import jakarta.persistence.LockModeType;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, String> {

    default TimeSlot findByIdOrThrow(String id) {
        return findById(id).orElseThrow(TimeSlotNotFoundException::new);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.id = :id")
    Optional<TimeSlot> findByIdForUpdate(@Param("id") String id);

    default TimeSlot findByIdForUpdateOrThrow(String id) {
        return findByIdForUpdate(id).orElseThrow(TimeSlotNotFoundException::new);
    }
}
