package com.ledgerx.repository;

import com.ledgerx.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}