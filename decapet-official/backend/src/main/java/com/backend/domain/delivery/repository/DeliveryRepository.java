package com.backend.domain.delivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.delivery.entity.Delivery;
import com.backend.domain.delivery.entity.DeliveryStatus;
import com.backend.domain.delivery.exception.DeliveryNotFoundException;

public interface DeliveryRepository extends JpaRepository<Delivery, String> {

    List<Delivery> findAllByUserId(String userId);

    Optional<Delivery> findByOrder_Id(String orderId);

    default Delivery getByIdOrThrow(String id) {
        return findById(id)
                .orElseThrow(DeliveryNotFoundException::new);
    }

    default Delivery getByOrderIdOrThrow(String orderId) {
        return findByOrder_Id(orderId)
                .orElseThrow(DeliveryNotFoundException::new);
    }

    boolean existsByTrackingNumber(String trackingNumber);

    @EntityGraph(attributePaths = {"order"})
    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);

    List<Delivery> findByOrderIdIn(List<String> orderIds);

    @Query(value = "SELECT * FROM deliveries WHERE user_id = :userId", nativeQuery = true)
    List<Delivery> findAllByUserIdIncludingDeleted(@Param("userId") String userId);
}
