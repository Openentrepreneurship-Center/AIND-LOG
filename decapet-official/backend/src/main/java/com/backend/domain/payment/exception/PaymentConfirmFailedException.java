package com.backend.domain.payment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PaymentConfirmFailedException extends BusinessException {

    public PaymentConfirmFailedException(String detail) {
        super(ErrorCode.PAYMENT_CONFIRM_FAILED);
        // detail은 TossPaymentsService에서 이미 log.error로 기록됨
    }
}
