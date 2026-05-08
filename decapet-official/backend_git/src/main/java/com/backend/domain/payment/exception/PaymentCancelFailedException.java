package com.backend.domain.payment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PaymentCancelFailedException extends BusinessException {

    public PaymentCancelFailedException(String paymentKey, int statusCode) {
        super(ErrorCode.PAYMENT_CANCEL_FAILED);
    }
}
