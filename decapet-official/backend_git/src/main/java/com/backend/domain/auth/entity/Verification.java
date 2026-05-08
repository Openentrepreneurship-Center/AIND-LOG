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

/**
 * Temporary verification codes for phone authentication.
 * Scheduled cleanup replaces MongoDB TTL index.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "verifications", indexes = {
    @Index(name = "idx_verifications_phone", columnList = "phone"),
    @Index(name = "idx_verifications_expires_at", columnList = "expires_at")
})
public class Verification {

    @Id
    @Column(length = 26)
    private String id;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(length = 255)
    private String token;

	@Column(nullable = false)
    private boolean tokenUsed;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime verifiedAt;

    @Builder
    public Verification(String phone, String code, int ttlMinutes) {
        this.id = UlidGenerator.generate();
        this.phone = phone;
        this.code = code;
        this.tokenUsed = false;
        this.createdAt = DateTimeUtil.now();
        this.expiresAt = this.createdAt.plusMinutes(ttlMinutes);
    }

    public void verify() {
        this.verifiedAt = DateTimeUtil.now();
        this.token = UlidGenerator.generate();
    }

    public void markTokenUsed() {
        this.tokenUsed = true;
    }

    public boolean isExpired() {
        return DateTimeUtil.now().isAfter(expiresAt);
    }
}
