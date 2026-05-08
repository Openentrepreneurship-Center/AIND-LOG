package com.backend.domain.customproduct.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.customproduct.entity.CustomProductStatus;
import com.backend.domain.customproduct.exception.CustomProductNotFoundException;

public interface CustomProductRepository extends JpaRepository<CustomProduct, String>, JpaSpecificationExecutor<CustomProduct> {

    @EntityGraph(attributePaths = {"user", "pet"})
    Page<CustomProduct> findAll(Specification<CustomProduct> spec, Pageable pageable);

    List<CustomProduct> findByPetIdAndDeletedAtIsNull(String petId);

    @EntityGraph(attributePaths = {"user", "pet"})
    Optional<CustomProduct> findByIdAndDeletedAtIsNull(String id);

    @EntityGraph(attributePaths = {"user", "pet"})
    Page<CustomProduct> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "pet"})
    Page<CustomProduct> findByStatusAndDeletedAtIsNull(CustomProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "pet"})
    Page<CustomProduct> findByDeletedAtIsNull(Pageable pageable);

    List<CustomProduct> findByUserIdAndStatusAndDeletedAtIsNull(String userId, CustomProductStatus status);

    @Query("SELECT cp FROM CustomProduct cp WHERE cp.status = :status AND cp.createdAt < :expirationThreshold AND cp.deletedAt IS NULL")
    List<CustomProduct> findExpiredApprovedProducts(
            @Param("status") CustomProductStatus status,
            @Param("expirationThreshold") LocalDateTime expirationThreshold);

    default CustomProduct findByIdOrThrow(String id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(CustomProductNotFoundException::new);
    }

    @Query(value = "SELECT * FROM custom_products WHERE pet_id = :petId", nativeQuery = true)
    List<CustomProduct> findByPetIdIncludingDeleted(@Param("petId") String petId);

    @Modifying
    @Query("UPDATE CustomProduct cp SET cp.stockQuantity = cp.stockQuantity - :quantity WHERE cp.id = :id AND cp.stockQuantity >= :quantity")
    int decreaseStockAtomically(@Param("id") String id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE CustomProduct cp SET cp.stockQuantity = cp.stockQuantity + :quantity WHERE cp.id = :id")
    int increaseStockAtomically(@Param("id") String id, @Param("quantity") int quantity);
}
