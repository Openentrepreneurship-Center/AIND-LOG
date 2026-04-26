package com.backend.domain.order.dto.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.backend.domain.delivery.entity.Delivery;
import com.backend.domain.delivery.repository.DeliveryRepository;
import com.backend.domain.order.dto.response.OrderItemResponse;
import com.backend.domain.order.dto.response.OrderListResponse;
import com.backend.domain.order.dto.response.OrderResponse;
import com.backend.domain.order.dto.response.ShippingAddressResponse;
import com.backend.domain.order.entity.Order;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderResponseMapper {

    private final OrderItemResponseMapper orderItemResponseMapper;
    private final DeliveryRepository deliveryRepository;

    public OrderResponse toResponse(Order order) {
        String trackingNumber = deliveryRepository.findByOrder_Id(order.getId())
                .map(Delivery::getTrackingNumber)
                .orElse(null);

        return toResponse(order, trackingNumber);
    }

    public OrderListResponse toListResponse(Page<Order> orderPage) {
        List<String> orderIds = orderPage.getContent().stream()
                .map(Order::getId)
                .toList();

        Map<String, String> trackingMap = deliveryRepository.findByOrderIdIn(orderIds).stream()
                .filter(d -> d.getTrackingNumber() != null)
                .collect(Collectors.toMap(
                        d -> d.getOrder().getId(),
                        Delivery::getTrackingNumber,
                        (a, b) -> a
                ));

        List<OrderResponse> orders = orderPage.getContent().stream()
                .map(order -> toResponse(order, trackingMap.get(order.getId())))
                .toList();

        return new OrderListResponse(
                orders,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.hasNext(),
                orderPage.hasPrevious()
        );
    }

    private OrderResponse toResponse(Order order, String trackingNumber) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(orderItemResponseMapper::toResponse)
                .toList();

        ShippingAddressResponse shippingAddress = new ShippingAddressResponse(
                order.getRecipientName(),
                order.getPhoneNumber(),
                order.getZipCode(),
                order.getAddress(),
                order.getDetailAddress(),
                order.getDeliveryNote()
        );

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                items,
                order.getTotalAmount(),
                order.getShippingFee(),
                order.getGrandTotal(),
                order.getStatus(),
                shippingAddress,
                order.isCancellable(),
                order.getCancelReason(),
                trackingNumber,
                order.getCreatedAt()
        );
    }
}
