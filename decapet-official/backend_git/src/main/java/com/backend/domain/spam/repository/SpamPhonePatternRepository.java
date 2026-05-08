package com.backend.domain.spam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.spam.entity.SpamPhonePattern;

public interface SpamPhonePatternRepository extends JpaRepository<SpamPhonePattern, Long> {

    boolean existsByPattern(String pattern);
}
