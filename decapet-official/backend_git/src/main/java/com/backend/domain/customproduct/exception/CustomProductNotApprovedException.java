package com.backend.domain.customproduct.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class CustomProductNotApprovedException extends BusinessException {
    public CustomProductNotApprovedException() {
        super(ErrorCode.CUSTOM_PRODUCT_NOT_APPROVED);
    }
}
