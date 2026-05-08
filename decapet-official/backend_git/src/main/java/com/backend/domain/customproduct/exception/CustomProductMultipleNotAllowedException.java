package com.backend.domain.customproduct.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class CustomProductMultipleNotAllowedException extends BusinessException {
    public CustomProductMultipleNotAllowedException() {
        super(ErrorCode.CUSTOM_PRODUCT_MULTIPLE_NOT_ALLOWED);
    }
}
