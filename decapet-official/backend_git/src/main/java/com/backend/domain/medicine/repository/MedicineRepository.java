package com.backend.domain.medicine.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.medicine.entity.Medicine;
import com.backend.domain.medicine.entity.MedicineSalesStatus;
import com.backend.domain.medicine.entity.MedicineType;
import com.backend.domain.medicine.exception.MedicineNotFoundException;

public interface MedicineRepository extends JpaRepository<Medicine, String> {

    @Query("SELECT m FROM Medicine m " +
           "WHERE (m.minWeight IS NULL OR m.minWeight <= :weight) " +
           "AND (m.maxWeight IS NULL OR m.maxWeight >= :weight)")
    Page<Medicine> findByWeightRange(@Param("weight") BigDecimal weight, Pageable pageable);

    @Query(value = "SELECT * FROM medicines WHERE deleted_at IS NULL ORDER BY RANDOM() LIMIT 4", nativeQuery = true)
    List<Medicine> findRandomFeaturedMedicines();

    Page<Medicine> findByType(MedicineType type, Pageable pageable);

    // Admin keyword search queries
    Page<Medicine> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Medicine> findByTypeAndNameContainingIgnoreCase(MedicineType type, String name, Pageable pageable);

    // User-facing queries (ON_SALE only)
    Page<Medicine> findBySalesStatus(MedicineSalesStatus salesStatus, Pageable pageable);

    Page<Medicine> findByTypeAndSalesStatus(MedicineType type, MedicineSalesStatus salesStatus, Pageable pageable);

    default Medicine findByIdOrThrow(String id) {
        return findById(id).orElseThrow(MedicineNotFoundException::new);
    }
}
