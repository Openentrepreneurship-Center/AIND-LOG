package com.backend.domain.auth.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class SmsSendFailedException extends BusinessException {
    public SmsSendFailedException() {
        super(ErrorCode.SMS_SEND_FAILED);
    }
}
