package com.backend.domain.medicine.exception;

import java.math.BigDecimal;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class WeightRangeMismatchException extends BusinessException {

    public WeightRangeMismatchException() {
        super(ErrorCode.WEIGHT_RANGE_MISMATCH);
    }

    public WeightRangeMismatchException(String medicineName, BigDecimal petWeight, String allowedRange) {
        super(ErrorCode.WEIGHT_RANGE_MISMATCH, String.format("반려동물 체중(%.1fkg)이 '%s' 의약품 적용 범위(%s)에 맞지 않습니다.",
                petWeight, medicineName, allowedRange));
    }
}
