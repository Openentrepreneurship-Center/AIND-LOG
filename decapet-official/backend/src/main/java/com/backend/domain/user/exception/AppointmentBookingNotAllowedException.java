package com.backend.domain.user.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class AppointmentBookingNotAllowedException extends BusinessException {
    public AppointmentBookingNotAllowedException() {
        super(ErrorCode.APPOINTMENT_BOOKING_NOT_ALLOWED);
    }
}
