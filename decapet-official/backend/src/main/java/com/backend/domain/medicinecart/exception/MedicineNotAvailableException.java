package com.backend.domain.medicinecart.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class MedicineNotAvailableException extends BusinessException {

    public MedicineNotAvailableException() {
        super(ErrorCode.MEDICINE_NOT_AVAILABLE);
    }
}
