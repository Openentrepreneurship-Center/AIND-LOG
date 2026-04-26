package com.backend.domain.order.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class OrderNotPendingException extends BusinessException {

    public OrderNotPendingException() {
        super(ErrorCode.ORDER_NOT_PENDING);
    }
}
