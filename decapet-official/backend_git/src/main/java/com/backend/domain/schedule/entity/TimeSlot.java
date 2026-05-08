package com.backend.domain.schedule.entity;

import java.time.LocalTime;

import com.backend.global.util.UlidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "time_slots", indexes = {
    @Index(name = "idx_time_slots_schedule", columnList = "schedule_id")
})
public class TimeSlot {

    @Id
    @Column(length = 26)
    private String id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, columnDefinition = "int default 1")
    private int maxCapacity = 1;

    @Builder
    public TimeSlot(LocalTime time, String location, int maxCapacity) {
        this.id = UlidGenerator.generate();
        this.time = time;
        this.location = location;
        this.maxCapacity = maxCapacity > 0 ? maxCapacity : 1;
    }

    public boolean isFull(int currentCount) {
        return currentCount >= maxCapacity;
    }

    public void updateMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity > 0 ? maxCapacity : 1;
    }
}
