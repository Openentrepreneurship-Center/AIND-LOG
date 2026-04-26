package com.backend.domain.customproduct.exception;

import com.backend.global.error.exception.BusinessException;
import com.backend.global.error.ErrorCode;

public class CustomProductNotFoundException extends BusinessException {
    public CustomProductNotFoundException() {
        super(ErrorCode.CUSTOM_PRODUCT_NOT_FOUND);
    }
}
