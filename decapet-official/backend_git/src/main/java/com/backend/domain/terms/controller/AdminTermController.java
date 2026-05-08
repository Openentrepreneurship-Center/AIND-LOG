package com.backend.domain.terms.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.terms.dto.request.CreateTermRequest;
import com.backend.domain.terms.dto.response.TermResponse;
import com.backend.domain.terms.entity.TermType;
import com.backend.domain.terms.service.AdminTermService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/terms")
@RequiredArgsConstructor
public class AdminTermController implements AdminTermApi {

    private final AdminTermService adminTermService;

    @Override
    @PostMapping
    public ResponseEntity<SuccessResponse> createTerm(
            @Valid @RequestBody CreateTermRequest request) {
        TermResponse response = adminTermService.createTerm(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_CREATE_SUCCESS, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getAllTerms(
            @RequestParam(required = false) TermType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<?> response = type != null
                ? adminTermService.getTermsByType(type, pageable)
                : adminTermService.getAllTerms(pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.TERM_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{termId}")
    public ResponseEntity<SuccessResponse> getTerm(@PathVariable String termId) {
        TermResponse response = adminTermService.getTerm(termId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.TERM_GET_SUCCESS, response));
    }
}
