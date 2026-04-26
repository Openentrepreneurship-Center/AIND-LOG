package com.backend.domain.schedule.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DuplicateTimeSlotException extends BusinessException {

    public DuplicateTimeSlotException() {
        super(ErrorCode.DUPLICATE_TIME_SLOT);
    }
}
