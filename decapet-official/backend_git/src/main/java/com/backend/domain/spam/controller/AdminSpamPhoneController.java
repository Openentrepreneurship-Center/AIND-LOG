package com.backend.domain.spam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.spam.dto.request.AddSpamPhonePatternRequest;
import com.backend.domain.spam.service.AdminSpamPhoneService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/spam-phones")
@RequiredArgsConstructor
public class AdminSpamPhoneController implements AdminSpamPhoneApi {

    private final AdminSpamPhoneService adminSpamPhoneService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getPatterns() {
        return ResponseEntity.ok(
            SuccessResponse.of(SuccessCode.SPAM_PHONE_LIST_SUCCESS, adminSpamPhoneService.getPatterns())
        );
    }

    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse> addPattern(@RequestBody @Valid AddSpamPhonePatternRequest request) {
        return ResponseEntity.status(201).body(
            SuccessResponse.of(SuccessCode.SPAM_PHONE_ADD_SUCCESS, adminSpamPhoneService.addPattern(request.pattern()))
        );
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deletePattern(@PathVariable Long id) {
        adminSpamPhoneService.deletePattern(id);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SPAM_PHONE_DELETE_SUCCESS));
    }
}
