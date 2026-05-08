package com.backend.domain.appointment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class AppointmentAlreadyRejectedException extends BusinessException {

    public AppointmentAlreadyRejectedException() {
        super(ErrorCode.APPOINTMENT_ALREADY_REJECTED);
    }
}
