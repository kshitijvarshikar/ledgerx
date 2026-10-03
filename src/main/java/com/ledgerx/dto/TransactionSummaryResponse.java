package com.ledgerx.dto;

import java.math.BigDecimal;

public class TransactionSummaryResponse {

    private final long totalTransactions;
    private final BigDecimal totalAmountTransferred;
    private final long successfulTransactions;
    private final long failedTransactions;

    public TransactionSummaryResponse(
            long totalTransactions,
            BigDecimal totalAmountTransferred,
            long successfulTransactions,
            long failedTransactions
    ) {
        this.totalTransactions = totalTransactions;
        this.totalAmountTransferred = totalAmountTransferred;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public BigDecimal getTotalAmountTransferred() {
        return totalAmountTransferred;
    }

    public long getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public long getFailedTransactions() {
        return failedTransactions;
    }
}