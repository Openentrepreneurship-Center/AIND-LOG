package com.backend.domain.medicinecart.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class MedicineCartNotFoundException extends BusinessException {

    public MedicineCartNotFoundException() {
        super(ErrorCode.MEDICINE_CART_NOT_FOUND);
    }
}
