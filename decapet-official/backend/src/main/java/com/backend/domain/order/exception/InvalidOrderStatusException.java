package com.backend.domain.order.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidOrderStatusException extends BusinessException {

    public InvalidOrderStatusException() {
        super(ErrorCode.INVALID_ORDER_STATUS);
    }
}
