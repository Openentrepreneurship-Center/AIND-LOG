package com.backend.domain.cart.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.cart.dto.response.CartItemResponse;
import com.backend.domain.cart.entity.CartItem;

@Component
public class CartItemResponseMapper {

    public CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
                item.getItemType(),
                item.getItemId(),
                item.getName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal(),
                item.isCustomProduct() && item.getCustomProduct() != null ? item.getCustomProduct().getWeight() : null,
                item.isCustomProduct() && item.getCustomProduct() != null ? item.getCustomProduct().getQuantity() : null,
                item.getImageUrl()
        );
    }
}
