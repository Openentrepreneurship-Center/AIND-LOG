package com.backend.domain.prescription.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PrescriptionValidationException extends BusinessException {

    public PrescriptionValidationException(String message) {
        super(ErrorCode.PRESCRIPTION_INVALID_INPUT, message);
    }
}
