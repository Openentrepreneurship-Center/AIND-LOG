package com.backend.domain.auth.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.auth.repository.TokenBlacklistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.backend.global.util.DateTimeUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlacklistCleanupScheduler {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    /**
     * 매시 30분마다 만료된 블랙리스트 항목 정리
     */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void cleanupExpiredBlacklistEntries() {
        try {
            tokenBlacklistRepository.deleteExpiredTokens(DateTimeUtil.now());
            log.info("Expired blacklist entries cleanup completed");
        } catch (Exception e) {
            log.error("블랙리스트 만료 항목 정리 실패", e);
        }
    }
}
