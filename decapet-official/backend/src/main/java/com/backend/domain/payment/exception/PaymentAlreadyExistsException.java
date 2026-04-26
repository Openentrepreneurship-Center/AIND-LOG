package com.backend.domain.payment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PaymentAlreadyExistsException extends BusinessException {

    public PaymentAlreadyExistsException() {
        super(ErrorCode.PAYMENT_ALREADY_EXISTS);
    }
}
