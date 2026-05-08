package com.backend.domain.cart.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class CartEmptyException extends BusinessException {

    public CartEmptyException() {
        super(ErrorCode.CART_EMPTY);
    }
}
