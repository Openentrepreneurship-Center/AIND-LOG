package com.backend.domain.schedule.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.global.util.UlidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "schedules")
@EntityListeners(AuditingEntityListener.class)
public class Schedule {

    @Id
    @Column(length = 26)
    private String id;

    @Column(nullable = false, unique = true)
    private LocalDate scheduleDate;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TimeSlot> timeSlots = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Schedule(LocalDate scheduleDate) {
        this.id = UlidGenerator.generate();
        this.scheduleDate = scheduleDate;
        this.timeSlots = new ArrayList<>();
    }

    public void addTimeSlot(TimeSlot timeSlot) {
        timeSlot.setSchedule(this);
        this.timeSlots.add(timeSlot);
    }

    public void removeTimeSlot(TimeSlot timeSlot) {
        this.timeSlots.remove(timeSlot);
        timeSlot.setSchedule(null);
    }
}
