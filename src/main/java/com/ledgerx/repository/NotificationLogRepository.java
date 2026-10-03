package com.ledgerx.repository;

import com.ledgerx.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByTransactionId(Long transactionId);
}