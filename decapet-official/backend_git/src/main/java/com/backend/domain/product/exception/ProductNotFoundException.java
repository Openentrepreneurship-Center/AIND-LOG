package com.backend.domain.product.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException() {
        super(ErrorCode.PRODUCT_NOT_FOUND);
    }

    public ProductNotFoundException(String message) {
        super(ErrorCode.PRODUCT_NOT_FOUND, message);
    }
}
