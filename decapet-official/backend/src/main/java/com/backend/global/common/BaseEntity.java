package com.backend.global.common;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.global.util.UlidGenerator;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import com.backend.global.util.DateTimeUtil;

/**
 * Base entity for all JPA entities.
 * Provides common fields: id (ULID), createdAt, updatedAt, deletedAt.
 * Implements Persistable so that save() calls persist() instead of merge()
 * for new entities (avoiding unnecessary SELECT before INSERT).
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Persistable<String> {

	@Id
	@Column(length = 26)
	private String id;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	protected BaseEntity() {
		this.id = UlidGenerator.generate();
	}

	@Override
	@Transient
	public boolean isNew() {
		return createdAt == null;
	}

	public void delete() {
		this.deletedAt = DateTimeUtil.now();
	}
}
