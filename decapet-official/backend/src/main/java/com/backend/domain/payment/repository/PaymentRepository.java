package com.backend.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.entity.PaymentStatus;
import com.backend.domain.payment.exception.PaymentNotFoundException;

import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    @EntityGraph(attributePaths = {"appointments", "order", "prescription"})
    Page<Payment> findByUserId(String userId, Pageable pageable);

    List<Payment> findAllByUserIdAndDeletedAtIsNull(String userId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p JOIN p.appointments a WHERE a.id = :appointmentId")
    boolean existsByAppointmentIds(@Param("appointmentId") String appointmentId);

    @Query("SELECT p FROM Payment p JOIN p.appointments a WHERE a.id = :appointmentId AND p.deletedAt IS NULL")
    Optional<Payment> findByAppointmentAndDeletedAtIsNull(@Param("appointmentId") String appointmentId);

    boolean existsByOrderIdAndStatus(String orderId, PaymentStatus status);

    Optional<Payment> findByOrderIdAndStatus(String orderId, PaymentStatus status);

    boolean existsByPrescriptionIdAndStatusNot(String prescriptionId, PaymentStatus status);

    Optional<Payment> findByPgTransactionId(String pgTransactionId);

    default Payment getById(String id) {
        return findById(id).orElseThrow(PaymentNotFoundException::new);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") String id);

    default Payment getByIdForUpdate(String id) {
        return findByIdForUpdate(id).orElseThrow(PaymentNotFoundException::new);
    }

    @Query(value = "SELECT * FROM payments WHERE user_id = :userId", nativeQuery = true)
    List<Payment> findAllByUserIdIncludingDeleted(@Param("userId") String userId);

    @Modifying
    @Query(value = "DELETE FROM payment_appointments WHERE appointment_id IN (:appointmentIds)", nativeQuery = true)
    void deletePaymentAppointmentsByAppointmentIds(@Param("appointmentIds") List<String> appointmentIds);
}
