package com.backend.domain.prescription.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PrescriptionPermissionDeniedException extends BusinessException {

    public PrescriptionPermissionDeniedException() {
        super(ErrorCode.PRESCRIPTION_PERMISSION_DENIED);
    }
}
