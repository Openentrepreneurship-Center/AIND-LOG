package com.backend.domain.cart.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.cart.entity.Cart;
import com.backend.domain.user.entity.User;

@Component
public class CartMapper {

    public Cart toEntity(User user) {
        return Cart.builder()
                .user(user)
                .build();
    }
}
