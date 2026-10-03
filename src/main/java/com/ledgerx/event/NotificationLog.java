package com.ledgerx.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private String transactionReference;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public NotificationLog() {
    }

    public Long getId() {
        return id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}