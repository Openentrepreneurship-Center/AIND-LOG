package com.backend.domain.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.admin.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}
