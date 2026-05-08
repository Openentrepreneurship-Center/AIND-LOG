package com.backend.domain.pet.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class PetPermissionDeniedException extends BusinessException {

    public PetPermissionDeniedException() {
        super(ErrorCode.PET_PERMISSION_DENIED);
    }
}
