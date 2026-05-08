package com.backend.domain.order.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.delivery.entity.Delivery;
import com.backend.domain.delivery.repository.DeliveryRepository;
import com.backend.domain.order.dto.mapper.OrderResponseMapper;
import com.backend.domain.order.dto.response.AdminOrderResponse;
import com.backend.domain.order.dto.response.OrderItemResponse;
import com.backend.domain.order.dto.response.OrderResponse;
import com.backend.domain.order.dto.response.ShippingAddressResponse;
import com.backend.domain.order.entity.Order;
import com.backend.domain.order.entity.OrderStatus;
import com.backend.domain.order.exception.OrderNotCancellableException;
import com.backend.domain.order.repository.OrderRepository;
import com.backend.global.common.PageResponse;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

	private final OrderRepository orderRepository;
	private final OrderResponseMapper orderResponseMapper;
	private final DeliveryRepository deliveryRepository;
	private final OrderService orderService;

	public PageResponse<AdminOrderResponse> getAllOrders(OrderStatus status, boolean includeDeleted,
			LocalDate startDate, LocalDate endDate, String searchText, String searchCategory, Pageable pageable) {

		Specification<Order> spec = buildSpecification(status, includeDeleted, startDate, endDate, searchText, searchCategory);
		Page<Order> orderPage = orderRepository.findAll(spec, pageable);

		List<String> orderIds = orderPage.getContent().stream()
				.map(Order::getId)
				.toList();

		Map<String, Delivery> deliveryMap = deliveryRepository.findByOrderIdIn(orderIds)
				.stream()
				.collect(Collectors.toMap(d -> d.getOrder().getId(), d -> d));

		return PageResponse.from(orderPage.map(order -> toAdminResponse(order, deliveryMap.get(order.getId()))));
	}

	private Specification<Order> buildSpecification(OrderStatus status, boolean includeDeleted,
			LocalDate startDate, LocalDate endDate, String searchText, String searchCategory) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (!includeDeleted) {
				predicates.add(cb.isNull(root.get("deletedAt")));
			}
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			} else {
				predicates.add(cb.notEqual(root.get("status"), OrderStatus.PENDING));
			}
			if (startDate != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
			}
			if (endDate != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
			}
			if (searchText != null && !searchText.isBlank()) {
				String like = "%" + searchText.toLowerCase() + "%";
				predicates.add(cb.or(
						cb.like(cb.lower(root.get("recipientName")), like),
						cb.like(root.get("phoneNumber"), like),
						cb.like(cb.lower(root.get("orderNumber")), like)
				));
			}

			// 최신 주문 우선 정렬
			if (query != null) {
				query.orderBy(cb.desc(root.get("createdAt")));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	@Transactional
	public void cancelOrder(String orderId, String reason) {
		Order order = orderRepository.getByIdOrThrow(orderId);
		if (!order.isPending()) {
			orderService.restoreProductStock(order);
		}
		order.cancel(reason);
	}

	@Transactional
	public void deleteOrder(String orderId) {
		Order order = orderRepository.getByIdOrThrow(orderId);
		if (order.getStatus() != OrderStatus.CANCELLED) {
			throw new OrderNotCancellableException();
		}
		order.delete();
	}

	public AdminOrderResponse getOrder(String orderId) {
		Order order = orderRepository.getByIdOrThrow(orderId);
		Delivery delivery = deliveryRepository.findByOrder_Id(orderId).orElse(null);
		return toAdminResponse(order, delivery);
	}

	private AdminOrderResponse toAdminResponse(Order order, Delivery delivery) {
		OrderResponse base = orderResponseMapper.toResponse(order);
		return new AdminOrderResponse(
				base.id(),
				base.orderNumber(),
				base.items(),
				base.totalAmount(),
				base.shippingFee(),
				base.grandTotal(),
				base.status(),
				base.shippingAddress(),
				base.cancellable(),
				base.cancelReason(),
				delivery != null ? delivery.getTrackingNumber() : null,
				delivery != null ? delivery.getId() : null,
				base.createdAt()
		);
	}
}
