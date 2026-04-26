package com.backend.domain.order.dto.response;

import java.math.BigDecimal;

import com.backend.global.common.ItemType;

public record OrderItemResponse(
        ItemType itemType,
        String itemId,
        String itemName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal,
        BigDecimal weight,
        Integer itemQuantity,
        String imageUrl
) {
}
