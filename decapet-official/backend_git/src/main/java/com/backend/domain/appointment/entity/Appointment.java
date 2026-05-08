package com.backend.domain.appointment.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.backend.domain.appointment.exception.AppointmentAlreadyApprovedException;
import com.backend.domain.appointment.exception.AppointmentAlreadyCompletedException;
import com.backend.domain.pet.entity.Pet;
import com.backend.domain.schedule.entity.TimeSlot;
import com.backend.domain.user.entity.User;
import com.backend.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appointments_user_status", columnList = "user_id, status"),
    @Index(name = "idx_appointments_time_slot", columnList = "time_slot_id, status")
})
@SQLRestriction("deleted_at IS NULL")
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @org.hibernate.annotations.BatchSize(size = 100)
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AppointmentMedicineItem> medicines = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus status;

    @Builder
    public Appointment(User user, Pet pet, TimeSlot timeSlot) {
        this.user = user;
        this.pet = pet;
        this.timeSlot = timeSlot;
        this.medicines = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.status = AppointmentStatus.PENDING;
    }

    public void addMedicine(AppointmentMedicineItem item) {
        item.setAppointment(this);
        this.medicines.add(item);
        recalculateTotalAmount();
    }

    private void recalculateTotalAmount() {
        this.totalAmount = medicines.stream()
                .map(AppointmentMedicineItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void approve() {
        this.status = AppointmentStatus.APPROVED;
    }

    public void approveFromPrescription() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new AppointmentAlreadyApprovedException();
        }
        this.status = AppointmentStatus.APPROVED;
    }

    public void reject() {
        this.status = AppointmentStatus.REJECTED;
    }

    public void revertToPending() {
        this.status = AppointmentStatus.PENDING;
    }

    public void detachTimeSlot() {
        this.timeSlot = null;
    }

    public void complete() {
        if (this.status != AppointmentStatus.APPROVED) {
            throw new AppointmentAlreadyCompletedException();
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    public boolean isPending() {
        return this.status == AppointmentStatus.PENDING;
    }
}
