package com.backend.domain.product.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class ProductMultipleNotAllowedException extends BusinessException {
    public ProductMultipleNotAllowedException() {
        super(ErrorCode.PRODUCT_MULTIPLE_NOT_ALLOWED);
    }
}
