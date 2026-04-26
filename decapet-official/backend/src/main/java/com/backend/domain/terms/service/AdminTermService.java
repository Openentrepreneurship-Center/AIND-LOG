package com.backend.domain.terms.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.terms.dto.mapper.TermMapper;
import com.backend.domain.terms.dto.mapper.TermResponseMapper;
import com.backend.domain.terms.dto.request.CreateTermRequest;
import com.backend.domain.terms.dto.response.TermResponse;
import com.backend.domain.terms.entity.Term;
import com.backend.domain.terms.entity.TermType;
import com.backend.domain.terms.exception.DuplicateTermVersionException;
import com.backend.domain.terms.repository.TermRepository;
import com.backend.global.common.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTermService {

    private final TermRepository termRepository;
    private final TermMapper termMapper;
    private final TermResponseMapper termResponseMapper;

    @Transactional
    public TermResponse createTerm(CreateTermRequest request) {
        termRepository.validateEffectiveDateOrThrow(request.type(), request.effectiveDate());
        int nextVersion = calculateNextVersion(request.type());

        Term term = termMapper.toEntity(request, nextVersion);
        try {
            Term savedTerm = termRepository.save(term);
            termRepository.flush();
            return termResponseMapper.toResponse(savedTerm);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateTermVersionException();
        }
    }

    public List<TermResponse> getAllTerms() {
        return termRepository.findAll().stream()
                .map(termResponseMapper::toResponse)
                .toList();
    }

    public List<TermResponse> getTermsByType(TermType type) {
        return termRepository.findByTypeOrderByVersionDesc(type).stream()
                .map(termResponseMapper::toResponse)
                .toList();
    }

    public PageResponse<TermResponse> getAllTerms(Pageable pageable) {
        return PageResponse.from(
                termRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(termResponseMapper::toResponse));
    }

    public PageResponse<TermResponse> getTermsByType(TermType type, Pageable pageable) {
        return PageResponse.from(
                termRepository.findByTypeOrderByVersionDesc(type, pageable)
                        .map(termResponseMapper::toResponse));
    }

    public TermResponse getTerm(String termId) {
        Term term = termRepository.findByIdOrThrow(termId);
        return termResponseMapper.toResponse(term);
    }

    private int calculateNextVersion(TermType type) {
        return termRepository.findFirstByTypeOrderByVersionDesc(type)
                .map(term -> term.getVersion() + 1)
                .orElse(1);
    }
}
