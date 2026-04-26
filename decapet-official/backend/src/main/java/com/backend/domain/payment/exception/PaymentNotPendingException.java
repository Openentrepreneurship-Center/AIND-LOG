package com.backend.domain.payment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PaymentNotPendingException extends BusinessException {

    public PaymentNotPendingException() {
        super(ErrorCode.PAYMENT_NOT_PENDING);
    }
}
