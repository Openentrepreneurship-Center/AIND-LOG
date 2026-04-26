package com.backend.domain.medicinecart.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class QuestionnaireNotCompletedException extends BusinessException {

    public QuestionnaireNotCompletedException() {
        super(ErrorCode.QUESTIONNAIRE_NOT_COMPLETED);
    }
}
