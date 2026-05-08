package com.backend.domain.order.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }

    public OrderNotFoundException(String message) {
        super(ErrorCode.ORDER_NOT_FOUND, message);
    }
}
