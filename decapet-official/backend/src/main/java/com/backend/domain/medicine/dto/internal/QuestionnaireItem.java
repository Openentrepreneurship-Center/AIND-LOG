package com.backend.domain.medicine.dto.internal;

import com.backend.global.util.UlidGenerator;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuestionnaireItem {

    private String questionId;
    private String question;

    @Builder
    public QuestionnaireItem(String questionId, String question) {
        this.questionId = questionId != null ? questionId : UlidGenerator.generate();
        this.question = question;
    }

    public static QuestionnaireItem of(String question) {
        return QuestionnaireItem.builder()
                .question(question)
                .build();
    }
}
