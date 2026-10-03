package com.ledgerx.service;

import com.ledgerx.entity.AuditLog;
import com.ledgerx.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(
            String userEmail,
            String action,
            String description
    ) {
        AuditLog auditLog = new AuditLog();

        auditLog.setUserEmail(userEmail);
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }
}