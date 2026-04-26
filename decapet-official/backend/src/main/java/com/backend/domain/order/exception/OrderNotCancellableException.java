package com.backend.domain.order.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class OrderNotCancellableException extends BusinessException {

    public OrderNotCancellableException() {
        super(ErrorCode.ORDER_NOT_CANCELLABLE);
    }
}
