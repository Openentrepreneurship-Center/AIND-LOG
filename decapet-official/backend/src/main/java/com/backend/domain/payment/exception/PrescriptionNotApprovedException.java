package com.backend.domain.payment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PrescriptionNotApprovedException extends BusinessException {

    public PrescriptionNotApprovedException() {
        super(ErrorCode.PRESCRIPTION_NOT_APPROVED);
    }
}
