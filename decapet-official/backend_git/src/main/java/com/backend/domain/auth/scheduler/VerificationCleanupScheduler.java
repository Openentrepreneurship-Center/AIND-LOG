package com.backend.domain.auth.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.auth.repository.VerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.backend.global.util.DateTimeUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCleanupScheduler {

    private final VerificationRepository verificationRepository;

    @Scheduled(cron = "0 0 * * * *") // 매시 정각
    @Transactional
    public void cleanupExpiredVerifications() {
        try {
            LocalDateTime now = DateTimeUtil.now();
            verificationRepository.deleteByExpiresAtBefore(now);
        } catch (Exception e) {
            log.error("만료 인증코드 정리 실패", e);
        }
    }
}
