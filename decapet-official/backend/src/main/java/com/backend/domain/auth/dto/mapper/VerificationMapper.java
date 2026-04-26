package com.backend.domain.auth.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.auth.dto.internal.VerificationCreateInfo;
import com.backend.domain.auth.entity.Verification;

@Component
public class VerificationMapper {

    public Verification toEntity(VerificationCreateInfo info) {
        return Verification.builder()
                .phone(info.phone())
                .code(info.code())
                .ttlMinutes(info.ttlMinutes())
                .build();
    }
}
