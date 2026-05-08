package com.backend.domain.cart.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.cart.dto.mapper.CartItemMapper;
import com.backend.domain.cart.dto.mapper.CartMapper;
import com.backend.domain.cart.dto.mapper.CartResponseMapper;
import com.backend.domain.cart.dto.request.AddToCartRequest;
import com.backend.domain.cart.dto.request.UpdateCartItemRequest;
import com.backend.domain.cart.dto.response.CartResponse;
import com.backend.domain.cart.entity.Cart;
import com.backend.domain.cart.entity.CartItem;
import com.backend.domain.cart.repository.CartRepository;
import com.backend.domain.customproduct.entity.CustomProduct;
import com.backend.domain.customproduct.entity.CustomProductStatus;
import com.backend.domain.customproduct.exception.CustomProductExpiredException;
import com.backend.domain.customproduct.exception.CustomProductMultipleNotAllowedException;
import com.backend.domain.customproduct.exception.CustomProductNotApprovedException;
import com.backend.domain.customproduct.exception.CustomProductNotOwnedException;
import com.backend.domain.customproduct.service.CustomProductService;
import com.backend.domain.product.entity.Product;
import com.backend.domain.product.exception.InsufficientStockException;
import com.backend.domain.product.exception.ProductMultipleNotAllowedException;
import com.backend.domain.product.exception.ProductNotAvailableException;
import com.backend.domain.product.service.ProductService;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.common.ItemType;

import lombok.RequiredArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final CartResponseMapper cartResponseMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductService productService;
    private final CustomProductService customProductService;

    @Transactional
    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return cartResponseMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(String userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem;

        if (request.itemType() == ItemType.PRODUCT) {
            cartItem = addProductToCart(request, cart);
        } else {
            cartItem = addCustomProductToCart(userId, request, cart);
        }

        cart.addItem(cartItem);
        return cartResponseMapper.toResponse(cart);
    }

    private CartItem addProductToCart(AddToCartRequest request, Cart cart) {
        Product product = productService.getByIdOrThrow(request.itemId());

        if (!product.isAvailable()) {
            throw new ProductNotAvailableException();
        }

        if (product.getStockQuantity() < request.quantity()) {
            throw new InsufficientStockException();
        }

        // allowMultiple이 false인 경우
        if (!product.getAllowMultiple()) {
            // 이미 장바구니에 있으면 추가 불가
            boolean alreadyInCart = cart.findItem(ItemType.PRODUCT, product.getId()).isPresent();
            if (alreadyInCart) {
                throw new ProductMultipleNotAllowedException();
            }
            // 수량이 1보다 크면 추가 불가
            if (request.quantity() > 1) {
                throw new ProductMultipleNotAllowedException();
            }
        }

        return cartItemMapper.toProductCartItem(product, request.quantity());
    }

    private CartItem addCustomProductToCart(String userId, AddToCartRequest request, Cart cart) {
        CustomProduct customProduct = customProductService.getByIdOrThrow(request.itemId());

        // 본인 것인지 확인
        if (customProduct.getUser() == null || !customProduct.getUser().getId().equals(userId)) {
            throw new CustomProductNotOwnedException();
        }

        // 승인 상태 확인
        if (customProduct.getStatus() != CustomProductStatus.APPROVED) {
            throw new CustomProductNotApprovedException();
        }

        // 만료 여부 확인 (승인일 + 1년)
        LocalDateTime expirationDate = customProduct.getExpirationDate();
        if (expirationDate == null || DateTimeUtil.now().isAfter(expirationDate)) {
            throw new CustomProductExpiredException();
        }

        // 재고 확인
        if (customProduct.getStockQuantity() < request.quantity()) {
            throw new InsufficientStockException();
        }

        // allowMultiple이 false인 경우
        if (!customProduct.getAllowMultiple()) {
            // 이미 장바구니에 있으면 추가 불가
            boolean alreadyInCart = cart.findItem(ItemType.CUSTOM_PRODUCT, customProduct.getId()).isPresent();
            if (alreadyInCart) {
                throw new CustomProductMultipleNotAllowedException();
            }
            // 수량이 1보다 크면 추가 불가
            if (request.quantity() > 1) {
                throw new CustomProductMultipleNotAllowedException();
            }
        }

        return cartItemMapper.toCustomProductCartItem(customProduct, request.quantity());
    }

    @Transactional
    public CartResponse updateCartItem(String userId, ItemType itemType, String itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        if (request.quantity() > 0) {
            if (itemType == ItemType.PRODUCT) {
                Product product = productService.getByIdOrThrow(itemId);
                if (product.getStockQuantity() < request.quantity()) {
                    throw new InsufficientStockException();
                }
                if (!product.getAllowMultiple() && request.quantity() > 1) {
                    throw new ProductMultipleNotAllowedException();
                }
            } else if (itemType == ItemType.CUSTOM_PRODUCT) {
                CustomProduct customProduct = customProductService.getByIdOrThrow(itemId);
                if (!customProduct.getAllowMultiple() && request.quantity() > 1) {
                    throw new CustomProductMultipleNotAllowedException();
                }
            }
        }

        cart.updateItemQuantity(itemType, itemId, request.quantity());
        return cartResponseMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(String userId, ItemType itemType, String itemId) {
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(itemType, itemId);
        return cartResponseMapper.toResponse(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
    }

    @Transactional
    public Cart getCartEntity(String userId) {
        User user = userRepository.getReferenceById(userId);
        return cartRepository.getOrCreate(userId, () -> cartMapper.toEntity(user));
    }

    private Cart getOrCreateCart(String userId) {
        User user = userRepository.getReferenceById(userId);
        return cartRepository.getOrCreate(userId, () -> cartMapper.toEntity(user));
    }
}
