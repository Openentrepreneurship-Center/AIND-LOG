package com.backend.domain.appointment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class TimeSlotFullException extends BusinessException {

    public TimeSlotFullException() {
        super(ErrorCode.TIME_SLOT_FULL);
    }
}
