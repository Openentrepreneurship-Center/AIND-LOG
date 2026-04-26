package com.backend.domain.admin.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;

import com.backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "admins")
@SQLRestriction("deleted_at IS NULL")
public class Admin extends BaseEntity {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String otpSecret;

    @Column(nullable = false)
    private boolean otpEnabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole role;

    @Column(nullable = false)
    private int failedLoginAttempts;

    private LocalDateTime lockedUntil;

    @Builder
    public Admin(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
        this.otpEnabled = false;
        this.role = AdminRole.ADMIN;
        this.failedLoginAttempts = 0;
    }

    public void setupOtp(String otpSecret) {
        this.otpSecret = otpSecret;
    }

    public void enableOtp() {
        this.otpEnabled = true;
    }

    public void disableOtp() {
        this.otpSecret = null;
        this.otpEnabled = false;
    }

    public boolean isLocked() {
        return lockedUntil != null && DateTimeUtil.now().isBefore(lockedUntil);
    }

    public void recordLoginFailure() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockedUntil = DateTimeUtil.now().plusMinutes(LOCK_DURATION_MINUTES);
        }
    }

    public void resetLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
