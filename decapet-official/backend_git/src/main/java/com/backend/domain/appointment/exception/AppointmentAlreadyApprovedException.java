package com.backend.domain.appointment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class AppointmentAlreadyApprovedException extends BusinessException {

    public AppointmentAlreadyApprovedException() {
        super(ErrorCode.APPOINTMENT_ALREADY_APPROVED);
    }
}
