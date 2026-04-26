package com.backend.domain.appointment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class AppointmentNotFoundException extends BusinessException {

    public AppointmentNotFoundException() {
        super(ErrorCode.APPOINTMENT_NOT_FOUND);
    }

    public AppointmentNotFoundException(String message) {
        super(ErrorCode.APPOINTMENT_NOT_FOUND, message);
    }
}
