package com.backend.domain.medicine.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class MonthsPerUnitRequiredException extends BusinessException {

    public MonthsPerUnitRequiredException() {
        super(ErrorCode.MONTHS_PER_UNIT_REQUIRED);
    }
}
