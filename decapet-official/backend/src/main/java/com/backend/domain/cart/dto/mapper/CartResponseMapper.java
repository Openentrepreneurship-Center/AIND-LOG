package com.backend.domain.cart.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.cart.dto.response.CartResponse;
import com.backend.domain.cart.entity.Cart;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartResponseMapper {

    private final CartItemResponseMapper cartItemResponseMapper;

    public CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getItems().stream()
                        .map(cartItemResponseMapper::toResponse)
                        .toList(),
                cart.getTotalAmount(),
                cart.getTotalQuantity(),
                cart.isEmpty()
        );
    }
}
