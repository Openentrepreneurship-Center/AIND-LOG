package com.backend.domain.prescription.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PrescriptionNotFoundException extends BusinessException {

    public PrescriptionNotFoundException() {
        super(ErrorCode.PRESCRIPTION_NOT_FOUND);
    }
}
