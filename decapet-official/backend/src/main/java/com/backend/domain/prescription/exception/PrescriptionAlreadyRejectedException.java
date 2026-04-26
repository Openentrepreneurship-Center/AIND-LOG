package com.backend.domain.prescription.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PrescriptionAlreadyRejectedException extends BusinessException {

    public PrescriptionAlreadyRejectedException() {
        super(ErrorCode.PRESCRIPTION_ALREADY_REJECTED);
    }
}
