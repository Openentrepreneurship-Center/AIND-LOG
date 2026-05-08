package com.backend.domain.appointment.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class QuestionnaireValidationException extends BusinessException {

    public QuestionnaireValidationException() {
        super(ErrorCode.QUESTIONNAIRE_VALIDATION_FAILED);
    }

    public QuestionnaireValidationException(String message) {
        super(ErrorCode.QUESTIONNAIRE_VALIDATION_FAILED, message);
    }
}
