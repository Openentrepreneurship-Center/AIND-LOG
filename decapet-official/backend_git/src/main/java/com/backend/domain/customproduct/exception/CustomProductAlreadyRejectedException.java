package com.backend.domain.customproduct.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class CustomProductAlreadyRejectedException extends BusinessException {
    public CustomProductAlreadyRejectedException() {
        super(ErrorCode.CUSTOM_PRODUCT_ALREADY_REJECTED);
    }
}
