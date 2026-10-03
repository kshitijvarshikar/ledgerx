package com.ledgerx.service;

import com.ledgerx.entity.Account;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.TransactionReportRepository;
import org.springframework.stereotype.Service;
import com.ledgerx.dto.TransactionSummaryResponse;

import java.time.LocalDateTime;

import java.math.BigDecimal;

@Service
public class TransactionReportService {

    private final TransactionReportRepository transactionReportRepository;
    private final AccountRepository accountRepository;

    public TransactionReportService(
            TransactionReportRepository transactionReportRepository,
            AccountRepository accountRepository
    ) {
        this.transactionReportRepository =
                transactionReportRepository;

        this.accountRepository =
                accountRepository;
    }

    public BigDecimal getTotalTransferredAmount(
            String userEmail,
            String accountNumber
    ) {

        Account account =
                accountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                ));

        if (!account.getUser().getEmail()
                .equals(userEmail)) {

            throw new IllegalArgumentException(
                    "You do not own this account"
            );
        }

        return transactionReportRepository
                .getTotalTransferredAmount(accountNumber);
    }

    public TransactionSummaryResponse getTransactionSummary(
            String userEmail,
            String accountNumber
    ) {

        Account account =
                accountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                ));

        if (!account.getUser().getEmail()
                .equals(userEmail)) {

            throw new IllegalArgumentException(
                    "You do not own this account"
            );
        }

        return transactionReportRepository
                .getTransactionSummary(accountNumber);
    }

    public TransactionSummaryResponse getTransactionSummaryByDateRange(
            String userEmail,
            String accountNumber,
            LocalDateTime from,
            LocalDateTime to
    ) {

        Account account =
                accountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                ));

        if (!account.getUser().getEmail()
                .equals(userEmail)) {

            throw new IllegalArgumentException(
                    "You do not own this account"
            );
        }

        return transactionReportRepository
                .getTransactionSummaryByDateRange(
                        accountNumber,
                        from,
                        to
                );
    }
}