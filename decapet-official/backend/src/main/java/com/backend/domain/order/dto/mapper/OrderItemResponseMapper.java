package com.backend.domain.order.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.order.dto.response.OrderItemResponse;
import com.backend.domain.order.entity.OrderItem;

@Component
public class OrderItemResponseMapper {

    public OrderItemResponse toResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getItemType(),
                item.getItemId(),
                item.getItemName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getWeight(),
                item.getItemQuantity(),
                item.getImageUrl()
        );
    }
}
