package com.backend.domain.admin.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.global.util.UlidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @Column(length = 26)
    private String id;

    @Column(nullable = false, length = 26)
    private String adminId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 50)
    private String targetType;

    @Column(length = 26)
    private String targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UlidGenerator.generate();
        }
    }

    @Builder
    public AuditLog(String adminId, String action, String targetType, String targetId,
                    Map<String, Object> details, String ipAddress) {
        this.adminId = adminId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.ipAddress = ipAddress;
    }
}
