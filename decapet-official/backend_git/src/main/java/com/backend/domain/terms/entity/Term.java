package com.backend.domain.terms.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.global.util.UlidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Terms are never deleted (legal evidence preservation).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "terms", uniqueConstraints = {
    @UniqueConstraint(name = "idx_terms_type_version", columnNames = {"type", "version"})
})
@EntityListeners(AuditingEntityListener.class)
public class Term {

    @Id
    @Column(length = 26)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TermType type;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isRequired;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Term(TermType type, int version, String content,
                boolean isRequired, LocalDate effectiveDate) {
        this.id = UlidGenerator.generate();
        this.type = type;
        this.version = version;
        this.content = content;
        this.isRequired = isRequired;
        this.effectiveDate = effectiveDate;
    }
}
