package com.backend.domain.payment.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.order.entity.Order;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.user.entity.User;
import com.backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_user", columnList = "user_id"),
    @Index(name = "idx_payments_order", columnList = "order_id"),
    @Index(name = "idx_payments_prescription", columnList = "prescription_id")
})
@SQLRestriction("deleted_at IS NULL")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType paymentType;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {jakarta.persistence.CascadeType.MERGE})
    @JoinTable(name = "payment_appointments",
            joinColumns = @JoinColumn(name = "payment_id"),
            inverseJoinColumns = @JoinColumn(name = "appointment_id"))
    private List<Appointment> appointments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String pgTransactionId;

    private String failureReason;

    @Column(length = 20)
    private String accountHolder;

    @Column(length = 10)
    private String cashReceiptType;

    @Column(length = 20)
    private String cashReceiptNumber;

    @Builder
    public Payment(User user, PaymentType paymentType, List<Appointment> appointments, Order order,
                   Prescription prescription, BigDecimal amount, PaymentMethod paymentMethod,
                   String accountHolder,
                   String cashReceiptType, String cashReceiptNumber) {
        this.user = user;
        this.paymentType = paymentType;
        this.appointments = appointments != null ? appointments : new ArrayList<>();
        this.order = order;
        this.prescription = prescription;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.paymentMethod = paymentMethod;
        this.accountHolder = accountHolder;
        this.cashReceiptType = cashReceiptType;
        this.cashReceiptNumber = cashReceiptNumber;
    }

    public boolean isAppointmentPayment() {
        return this.paymentType == PaymentType.APPOINTMENT;
    }

    public Appointment getFirstAppointment() {
        return this.appointments.isEmpty() ? null : this.appointments.get(0);
    }

    public boolean isOrderPayment() {
        return this.paymentType == PaymentType.ORDER;
    }

    public boolean isPrescriptionPayment() {
        return this.paymentType == PaymentType.PRESCRIPTION;
    }

    public void savePgPaymentKey(String pgTransactionId) {
        this.pgTransactionId = pgTransactionId;
    }

    public void complete(String pgTransactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.pgTransactionId = pgTransactionId;
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    public void anonymize() {
        this.accountHolder = null;
        this.cashReceiptType = null;
        this.cashReceiptNumber = null;
    }
}
