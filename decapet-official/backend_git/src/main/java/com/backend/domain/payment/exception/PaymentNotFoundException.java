package com.backend.domain.payment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PaymentNotFoundException extends BusinessException {

    public PaymentNotFoundException() {
        super(ErrorCode.PAYMENT_NOT_FOUND);
    }
}
