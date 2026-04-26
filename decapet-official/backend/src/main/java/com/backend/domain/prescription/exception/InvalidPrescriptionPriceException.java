package com.backend.domain.prescription.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class InvalidPrescriptionPriceException extends BusinessException {

    public InvalidPrescriptionPriceException() {
        super(ErrorCode.INVALID_PRESCRIPTION_PRICE);
    }
}
