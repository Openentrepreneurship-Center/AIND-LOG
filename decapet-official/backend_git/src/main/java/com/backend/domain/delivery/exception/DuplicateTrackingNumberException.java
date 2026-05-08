package com.backend.domain.delivery.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class DuplicateTrackingNumberException extends BusinessException {

    public DuplicateTrackingNumberException() {
        super(ErrorCode.DUPLICATE_TRACKING_NUMBER);
    }

    public DuplicateTrackingNumberException(String trackingNumber) {
        super(ErrorCode.DUPLICATE_TRACKING_NUMBER, "중복된 운송장 번호입니다: " + trackingNumber);
    }
}
