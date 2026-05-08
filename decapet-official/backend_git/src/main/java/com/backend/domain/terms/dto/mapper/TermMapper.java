package com.backend.domain.terms.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.terms.dto.request.CreateTermRequest;
import com.backend.domain.terms.entity.Term;

@Component
public class TermMapper {

    public Term toEntity(CreateTermRequest request, int version) {
        return Term.builder()
                .type(request.type())
                .version(version)
                .content(request.content())
                .isRequired(request.isRequired())
                .effectiveDate(request.effectiveDate())
                .build();
    }
}
