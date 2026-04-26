package com.backend.domain.cart.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.cart.entity.CartItem;
import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.product.entity.Product;

@Component
public class CartItemMapper {

    public CartItem toProductCartItem(Product product, int quantity) {
        return CartItem.createProductItem(product, quantity);
    }

    public CartItem toCustomProductCartItem(CustomProduct customProduct, int quantity) {
        return CartItem.createCustomProductItem(customProduct, quantity);
    }
}
