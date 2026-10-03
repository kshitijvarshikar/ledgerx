package com.ledgerx.service;

import com.ledgerx.entity.AuditLog;
import com.ledgerx.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void shouldCreateAuditLog() {

        auditService.log(
                "john@example.com",
                "TRANSFER",
                "Money transferred successfully"
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository)
                .save(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertEquals(
                "john@example.com",
                auditLog.getUserEmail()
        );

        assertEquals(
                "TRANSFER",
                auditLog.getAction()
        );

        assertEquals(
                "Money transferred successfully",
                auditLog.getDescription()
        );

        assertNotNull(auditLog.getCreatedAt());
    }

    @Test
    void shouldSaveAuditLogOnlyOnce() {

        auditService.log(
                "alice@example.com",
                "LOGIN",
                "User logged in"
        );

        verify(auditLogRepository, times(1))
                .save(any(AuditLog.class));
    }

    @Test
    void shouldStoreProvidedAuditDetails() {

        auditService.log(
                "bob@example.com",
                "ACCOUNT_CREATED",
                "New account created"
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository)
                .save(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertEquals("bob@example.com",
                auditLog.getUserEmail());

        assertEquals("ACCOUNT_CREATED",
                auditLog.getAction());

        assertEquals("New account created",
                auditLog.getDescription());
    }
}