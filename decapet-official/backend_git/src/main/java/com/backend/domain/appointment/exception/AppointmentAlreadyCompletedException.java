package com.backend.domain.appointment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class AppointmentAlreadyCompletedException extends BusinessException {

    public AppointmentAlreadyCompletedException() {
        super(ErrorCode.APPOINTMENT_ALREADY_COMPLETED);
    }
}
