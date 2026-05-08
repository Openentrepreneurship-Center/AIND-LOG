package com.backend.domain.schedule.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class TimeSlotNotFoundException extends BusinessException {

    public TimeSlotNotFoundException() {
        super(ErrorCode.TIME_SLOT_NOT_FOUND);
    }
}
