package com.backend.domain.spam.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.spam.dto.response.SpamPhonePatternResponse;
import com.backend.domain.spam.entity.SpamPhonePattern;
import com.backend.domain.spam.repository.SpamPhonePatternRepository;
import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSpamPhoneService {

    private final SpamPhonePatternRepository spamPhonePatternRepository;

    public List<SpamPhonePatternResponse> getPatterns() {
        return spamPhonePatternRepository.findAll().stream()
            .map(p -> new SpamPhonePatternResponse(p.getId(), p.getPattern(), p.getCreatedAt()))
            .toList();
    }

    @Transactional
    public SpamPhonePatternResponse addPattern(String rawPattern) {
        String pattern = rawPattern.replaceAll("[^0-9]", "");
        if (pattern.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (spamPhonePatternRepository.existsByPattern(pattern)) {
            throw new BusinessException(ErrorCode.DUPLICATE_SPAM_PATTERN);
        }
        SpamPhonePattern entity = spamPhonePatternRepository.save(new SpamPhonePattern(pattern));
        return new SpamPhonePatternResponse(entity.getId(), entity.getPattern(), entity.getCreatedAt());
    }

    @Transactional
    public void deletePattern(Long id) {
        if (!spamPhonePatternRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        spamPhonePatternRepository.deleteById(id);
    }

    public boolean isSpamPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String normalized = phone.replaceAll("[^0-9]", "");
        List<SpamPhonePattern> patterns = spamPhonePatternRepository.findAll();
        return patterns.stream().anyMatch(p -> normalized.contains(p.getPattern()));
    }
}
