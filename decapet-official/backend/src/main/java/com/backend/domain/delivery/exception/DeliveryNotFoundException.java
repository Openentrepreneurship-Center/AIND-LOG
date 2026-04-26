package com.backend.domain.delivery.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DeliveryNotFoundException extends BusinessException {

    public DeliveryNotFoundException() {
        super(ErrorCode.DELIVERY_NOT_FOUND);
    }

    public DeliveryNotFoundException(String message) {
        super(ErrorCode.DELIVERY_NOT_FOUND, message);
    }
}
