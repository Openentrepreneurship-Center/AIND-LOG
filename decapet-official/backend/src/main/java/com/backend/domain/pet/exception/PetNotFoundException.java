package com.backend.domain.pet.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PetNotFoundException extends BusinessException {

    public PetNotFoundException() {
        super(ErrorCode.PET_NOT_FOUND);
    }
}
