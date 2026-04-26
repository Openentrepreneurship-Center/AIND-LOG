package com.backend.domain.terms.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.terms.entity.Term;
import com.backend.domain.user.entity.UserTermConsent;

@Component
public class UserTermConsentMapper {

    public UserTermConsent toEntity(Term term) {
        return UserTermConsent.builder()
                .termId(term.getId())
                .version(term.getVersion())
                .build();
    }
}
