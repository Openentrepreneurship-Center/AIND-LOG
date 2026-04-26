package com.backend.domain.appointment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DuplicateUserAppointmentException extends BusinessException {

    public DuplicateUserAppointmentException() {
        super(ErrorCode.DUPLICATE_USER_APPOINTMENT);
    }
}
