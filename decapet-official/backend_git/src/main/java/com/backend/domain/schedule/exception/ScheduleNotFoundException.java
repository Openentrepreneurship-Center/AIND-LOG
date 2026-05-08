package com.backend.domain.schedule.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class ScheduleNotFoundException extends BusinessException {

    public ScheduleNotFoundException() {
        super(ErrorCode.SCHEDULE_NOT_FOUND);
    }
}
