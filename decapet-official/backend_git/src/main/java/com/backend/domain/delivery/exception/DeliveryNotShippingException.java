package com.backend.domain.delivery.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DeliveryNotShippingException extends BusinessException {
    public DeliveryNotShippingException() {
        super(ErrorCode.INVALID_DELIVERY_STATUS);
    }
}
