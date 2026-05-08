package com.backend.domain.cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.cart.dto.request.AddToCartRequest;
import com.backend.domain.cart.dto.request.UpdateCartItemRequest;
import com.backend.domain.cart.dto.response.CartResponse;
import com.backend.domain.cart.service.CartService;
import com.backend.global.common.ItemType;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController implements CartApi {

    private final CartService cartService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getCart(@AuthenticationPrincipal String userId) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CART_GET_SUCCESS, response));
    }

    @Override
    @PostMapping("/items")
    public ResponseEntity<SuccessResponse> addToCart(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid AddToCartRequest request) {
        CartResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CART_ADD_SUCCESS, response));
    }

    @Override
    @PatchMapping("/items/{itemType}/{itemId}")
    public ResponseEntity<SuccessResponse> updateCartItem(
            @AuthenticationPrincipal String userId,
            @PathVariable ItemType itemType,
            @PathVariable String itemId,
            @RequestBody @Valid UpdateCartItemRequest request) {
        CartResponse response = cartService.updateCartItem(userId, itemType, itemId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CART_UPDATE_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/items/{itemType}/{itemId}")
    public ResponseEntity<SuccessResponse> removeCartItem(
            @AuthenticationPrincipal String userId,
            @PathVariable ItemType itemType,
            @PathVariable String itemId) {
        CartResponse response = cartService.removeCartItem(userId, itemType, itemId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CART_REMOVE_SUCCESS, response));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<SuccessResponse> clearCart(@AuthenticationPrincipal String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.CART_CLEAR_SUCCESS));
    }
}
