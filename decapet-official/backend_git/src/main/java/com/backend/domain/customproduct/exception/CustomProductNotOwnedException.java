package com.backend.domain.customproduct.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class CustomProductNotOwnedException extends BusinessException {
    public CustomProductNotOwnedException() {
        super(ErrorCode.CUSTOM_PRODUCT_NOT_OWNED);
    }
}
