package com.backend.domain.appointment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DuplicateAppointmentException extends BusinessException {

    public DuplicateAppointmentException() {
        super(ErrorCode.DUPLICATE_APPOINTMENT);
    }
}
