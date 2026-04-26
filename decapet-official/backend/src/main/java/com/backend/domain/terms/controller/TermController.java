package com.backend.domain.terms.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.terms.dto.request.TermConsentRequest;
import com.backend.domain.terms.dto.response.TermConsentStatusResponse;
import com.backend.domain.terms.dto.response.TermResponse;
import com.backend.domain.terms.service.TermService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermController implements TermApi {

    private final TermService termService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getCurrentTerms() {
        List<TermResponse> terms = termService.getCurrentTerms();
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.TERM_LIST_SUCCESS, terms));
    }

    @Override
    @PostMapping("/consent")
    public ResponseEntity<SuccessResponse> acceptTerms(
            @AuthenticationPrincipal String userId,
            @RequestBody @Valid TermConsentRequest request) {
        termService.acceptTerms(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.TERM_CONSENT_SUCCESS));
    }

    @Override
    @GetMapping("/consent/status")
    public ResponseEntity<SuccessResponse> getConsentStatus(
            @AuthenticationPrincipal String userId) {
        TermConsentStatusResponse response = termService.getConsentStatus(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.TERM_GET_SUCCESS, response));
    }
}
