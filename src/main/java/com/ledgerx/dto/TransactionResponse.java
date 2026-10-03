package com.ledgerx.dto;

import com.ledgerx.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public TransactionResponse() {
    }

    public TransactionResponse(
            Long id,
            String transactionReference,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            TransactionStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}