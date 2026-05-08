package com.backend.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.auth.entity.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, String> {

    boolean existsByJti(String jti);

    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") java.time.LocalDateTime now);
}
