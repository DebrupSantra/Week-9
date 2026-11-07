package com.example.ecommerce.repository;

import com.example.ecommerce.security.audit.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
}
