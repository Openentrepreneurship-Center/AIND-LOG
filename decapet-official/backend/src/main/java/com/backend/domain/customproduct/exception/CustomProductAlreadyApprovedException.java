package com.backend.domain.customproduct.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class CustomProductAlreadyApprovedException extends BusinessException {
    public CustomProductAlreadyApprovedException() {
        super(ErrorCode.CUSTOM_PRODUCT_ALREADY_APPROVED);
    }
}
