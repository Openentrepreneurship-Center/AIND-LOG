package com.backend.domain.schedule.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class TimeSlotExpiredException extends BusinessException {

    public TimeSlotExpiredException() {
        super(ErrorCode.TIME_SLOT_EXPIRED);
    }
}
