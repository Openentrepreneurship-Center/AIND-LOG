package com.backend.domain.medicinecart.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class MedicineCartEmptyException extends BusinessException {

    public MedicineCartEmptyException() {
        super(ErrorCode.MEDICINE_CART_EMPTY);
    }
}
