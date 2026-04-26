package com.backend.domain.medicine.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class MonthsPerUnitNotAllowedException extends BusinessException {

    public MonthsPerUnitNotAllowedException() {
        super(ErrorCode.MONTHS_PER_UNIT_NOT_ALLOWED);
    }
}
