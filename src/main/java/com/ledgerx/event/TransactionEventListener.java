package com.ledgerx.event;

import com.ledgerx.entity.NotificationLog;
import com.ledgerx.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
public class TransactionEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionEventListener.class);

    private final NotificationLogRepository notificationLogRepository;

    public TransactionEventListener(
            NotificationLogRepository notificationLogRepository
    ) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Async("ledgerTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCompleted(
            TransactionCompletedEvent event
    ) {

        log.info(
                "Async transaction processing started: {}",
                event.getTransactionReference()
        );

        NotificationLog notification =
                new NotificationLog();

        notification.setTransactionId(
                event.getTransactionId()
        );

        notification.setTransactionReference(
                event.getTransactionReference()
        );

        notification.setRecipientEmail(
                event.getReceiverEmail()
        );

        notification.setMessage(
                "Transaction "
                        + event.getTransactionReference()
                        + " completed successfully"
        );

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        notificationLogRepository.save(notification);

        log.info(
                "Notification log saved successfully for transaction: {}",
                event.getTransactionReference()
        );

        log.info(
                "Transaction {} processed by thread: {}",
                event.getTransactionReference(),
                Thread.currentThread().getName()
        );

        log.info(
                "Async transaction processing completed: {}",
                event.getTransactionReference()
        );
    }
}