package com.backend.domain.auth.entity;

import java.time.LocalDateTime;

import com.backend.global.util.UlidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    @Column(length = 26)
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    private String jti;

    @Column(nullable = false, length = 255)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public TokenBlacklist(String jti, String userId, LocalDateTime expiresAt) {
        this.id = UlidGenerator.generate();
        this.jti = jti;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.createdAt = DateTimeUtil.now();
    }

    public boolean isExpired() {
        return DateTimeUtil.now().isAfter(expiresAt);
    }
}
