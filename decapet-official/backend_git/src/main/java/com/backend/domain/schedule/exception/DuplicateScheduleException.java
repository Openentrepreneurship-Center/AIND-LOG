package com.backend.domain.schedule.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DuplicateScheduleException extends BusinessException {

    public DuplicateScheduleException() {
        super(ErrorCode.DUPLICATE_SCHEDULE);
    }
}
