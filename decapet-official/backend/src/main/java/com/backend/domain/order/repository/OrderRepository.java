package com.backend.domain.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.backend.domain.order.entity.Order;
import com.backend.domain.order.entity.OrderStatus;
import com.backend.domain.order.exception.OrderNotFoundException;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

	@Query(value = "SELECT MAX(order_number) FROM orders WHERE order_number LIKE :prefix || '%'", nativeQuery = true)
	Optional<String> findMaxOrderNumberByPrefix(@Param("prefix") String prefix);

	@Query(value = "SELECT EXISTS(SELECT 1 FROM orders WHERE order_number = :orderNumber)", nativeQuery = true)
	boolean existsByOrderNumberIncludingDeleted(@Param("orderNumber") String orderNumber);

	default Order getByIdOrThrow(String id) {
		return findById(id)
			.orElseThrow(OrderNotFoundException::new);
	}

	Optional<Order> findByOrderNumber(String orderNumber);

	default Order getByOrderNumberOrThrow(String orderNumber) {
		return findByOrderNumber(orderNumber)
			.orElseThrow(OrderNotFoundException::new);
	}

	List<Order> findAllByUserId(String userId);

	Page<Order> findByUserId(String userId, Pageable pageable);

	Page<Order> findByUserIdAndStatusNot(String userId, OrderStatus status, Pageable pageable);

	Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);

	@EntityGraph(attributePaths = {"items"})
	List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime expiredTime);

	List<Order> findByStatusAndUpdatedAtBefore(OrderStatus status, LocalDateTime before);

	Page<Order> findByStatus(OrderStatus status, Pageable pageable);

	@Query(value = "SELECT * FROM orders ORDER BY created_at DESC",
		   countQuery = "SELECT count(*) FROM orders",
		   nativeQuery = true)
	Page<Order> findAllIncludingDeleted(Pageable pageable);

	@Query(value = "SELECT * FROM orders WHERE status = :status ORDER BY created_at DESC",
		   countQuery = "SELECT count(*) FROM orders WHERE status = :status",
		   nativeQuery = true)
	Page<Order> findByStatusIncludingDeleted(@Param("status") String status, Pageable pageable);

	@Query(value = "SELECT * FROM orders WHERE user_id = :userId", nativeQuery = true)
	List<Order> findAllByUserIdIncludingDeleted(@Param("userId") String userId);
}
