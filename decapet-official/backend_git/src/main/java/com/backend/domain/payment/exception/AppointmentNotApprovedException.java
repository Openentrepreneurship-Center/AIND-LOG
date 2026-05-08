package com.backend.domain.payment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class AppointmentNotApprovedException extends BusinessException {

    public AppointmentNotApprovedException() {
        super(ErrorCode.APPOINTMENT_NOT_APPROVED);
    }
}
