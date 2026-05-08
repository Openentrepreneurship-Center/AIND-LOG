package com.backend.domain.auth.entity;

import java.time.LocalDateTime;

import com.backend.global.util.UlidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
})
public class RefreshToken {

    @Id
    @Column(length = 26)
    private String id;

    @Column(nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public RefreshToken(String userId, String tokenHash, int ttlDays) {
        this.id = UlidGenerator.generate();
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = DateTimeUtil.now();
        this.expiresAt = this.createdAt.plusDays(ttlDays);
    }

    public boolean isExpired() {
        return DateTimeUtil.now().isAfter(expiresAt);
    }
}
