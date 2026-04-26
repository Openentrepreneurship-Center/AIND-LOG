package com.backend.domain.pet.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class WeightUpdateRestrictedException extends BusinessException {

    public WeightUpdateRestrictedException() {
        super(ErrorCode.WEIGHT_UPDATE_RESTRICTED);
    }
}
