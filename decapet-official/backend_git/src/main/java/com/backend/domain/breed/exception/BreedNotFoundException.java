package com.backend.domain.breed.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class BreedNotFoundException extends BusinessException {

    public BreedNotFoundException() {
        super(ErrorCode.BREED_NOT_FOUND);
    }
}
