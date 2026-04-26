package com.backend.domain.customproduct.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class CustomProductExpiredException extends BusinessException {
    public CustomProductExpiredException() {
        super(ErrorCode.CUSTOM_PRODUCT_EXPIRED);
    }
}
