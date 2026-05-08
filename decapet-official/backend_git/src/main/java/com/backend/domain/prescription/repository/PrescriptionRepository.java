package com.backend.domain.prescription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.prescription.entity.PrescriptionStatus;
import com.backend.domain.prescription.exception.PrescriptionNotFoundException;

public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

    List<Prescription> findByPetIdAndDeletedAtIsNull(String petId);

    Optional<Prescription> findByIdAndDeletedAtIsNull(String id);

    default Prescription findByIdOrThrow(String id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(PrescriptionNotFoundException::new);
    }

    @EntityGraph(attributePaths = {"pet", "user"})
    Page<Prescription> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    @EntityGraph(attributePaths = {"pet", "user"})
    Page<Prescription> findByStatusAndDeletedAtIsNull(PrescriptionStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"pet", "user"})
    Page<Prescription> findByDeletedAtIsNull(Pageable pageable);

    List<Prescription> findByUserIdAndStatusAndDeletedAtIsNull(String userId, PrescriptionStatus status);

    @Query(value = "SELECT * FROM prescriptions WHERE pet_id = :petId", nativeQuery = true)
    List<Prescription> findByPetIdIncludingDeleted(@Param("petId") String petId);

    @EntityGraph(attributePaths = {"pet", "user"})
    @Query("SELECT p FROM Prescription p " +
           "LEFT JOIN p.user u " +
           "WHERE p.deletedAt IS NULL " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR u.phone LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Prescription> searchByKeyword(@Param("status") PrescriptionStatus status,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);
}
