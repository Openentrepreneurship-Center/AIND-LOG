package com.backend.domain.medicine.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class MedicineNotFoundException extends BusinessException {

    public MedicineNotFoundException() {
        super(ErrorCode.MEDICINE_NOT_FOUND);
    }

    public MedicineNotFoundException(String message) {
        super(ErrorCode.MEDICINE_NOT_FOUND, message);
    }
}
