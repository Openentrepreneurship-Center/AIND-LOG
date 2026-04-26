package com.backend.domain.spam.controller;

import org.springframework.http.ResponseEntity;

import com.backend.domain.spam.dto.request.AddSpamPhonePatternRequest;
import com.backend.global.common.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[Admin] 스팸 전화번호", description = "스팸 전화번호 패턴 관리")
public interface AdminSpamPhoneApi {

    @Operation(summary = "스팸 전화번호 패턴 목록 조회")
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> getPatterns();

    @Operation(summary = "스팸 전화번호 패턴 등록")
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> addPattern(@Valid AddSpamPhonePatternRequest request);

    @Operation(summary = "스팸 전화번호 패턴 삭제")
    @SecurityRequirement(name = "cookieAuth")
    ResponseEntity<SuccessResponse> deletePattern(Long id);
}
