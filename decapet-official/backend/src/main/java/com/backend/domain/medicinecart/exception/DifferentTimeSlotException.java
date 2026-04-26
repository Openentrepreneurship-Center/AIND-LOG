package com.backend.domain.medicinecart.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DifferentTimeSlotException extends BusinessException {

    public DifferentTimeSlotException() {
        super(ErrorCode.DIFFERENT_TIME_SLOT);
    }
}
