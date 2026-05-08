package com.backend.domain.appointment.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionnaireAnswer {

    private String questionId;
    private boolean answer;

    @Builder
    public QuestionnaireAnswer(String questionId, boolean answer) {
        this.questionId = questionId;
        this.answer = answer;
    }
}
