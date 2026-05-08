package com.backend.domain.pet.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PetVetNotFoundException extends BusinessException {

    public PetVetNotFoundException() {
        super(ErrorCode.PET_VET_NOT_FOUND);
    }
}
