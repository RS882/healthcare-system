package com.healthcare.user_service.audit.repository;

import com.healthcare.user_service.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
