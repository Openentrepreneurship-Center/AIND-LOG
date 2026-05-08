package com.backend.domain.product.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class ProductNotAvailableException extends BusinessException {

    public ProductNotAvailableException() {
        super(ErrorCode.PRODUCT_NOT_AVAILABLE);
    }
}
