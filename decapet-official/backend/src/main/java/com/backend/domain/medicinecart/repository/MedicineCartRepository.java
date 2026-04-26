package com.backend.domain.medicinecart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.medicinecart.entity.MedicineCart;

import jakarta.persistence.LockModeType;

public interface MedicineCartRepository extends JpaRepository<MedicineCart, String> {

    Optional<MedicineCart> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM MedicineCart c WHERE c.user.id = :userId")
    Optional<MedicineCart> findByUserIdForUpdate(@Param("userId") String userId);

    @Modifying
    @Query(value = "UPDATE medicine_carts SET time_slot_id = NULL WHERE time_slot_id = :timeSlotId", nativeQuery = true)
    void clearTimeSlotId(@Param("timeSlotId") String timeSlotId);
}
