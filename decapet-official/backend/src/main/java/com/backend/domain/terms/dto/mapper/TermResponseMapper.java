package com.backend.domain.terms.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.terms.dto.response.TermResponse;
import com.backend.domain.terms.entity.Term;

@Component
public class TermResponseMapper {

    public TermResponse toResponse(Term term) {
        return new TermResponse(
                term.getId(),
                term.getType(),
                term.getVersion(),
                term.getContent(),
                term.isRequired(),
                term.getEffectiveDate()
        );
    }
}
