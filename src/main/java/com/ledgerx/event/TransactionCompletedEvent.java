package com.ledgerx.event;

public class TransactionCompletedEvent {

    private final Long transactionId;
    private final String transactionReference;
    private final String senderEmail;
    private final String receiverEmail;
    private final String senderAccountNumber;
    private final String receiverAccountNumber;

    public TransactionCompletedEvent(
            Long transactionId,
            String transactionReference,
            String senderEmail,
            String receiverEmail,
            String senderAccountNumber,
            String receiverAccountNumber
    ) {
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }
}