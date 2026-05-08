package com.backend.domain.prescription.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PrescriptionAlreadyApprovedException extends BusinessException {

    public PrescriptionAlreadyApprovedException() {
        super(ErrorCode.PRESCRIPTION_ALREADY_APPROVED);
    }
}
