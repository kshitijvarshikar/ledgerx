package com.ledgerx.service;

import com.ledgerx.dto.TransactionResponse;
import com.ledgerx.dto.TransferRequest;
import com.ledgerx.entity.Account;
import com.ledgerx.entity.AccountStatus;
import com.ledgerx.entity.Transaction;
import com.ledgerx.entity.TransactionStatus;
import com.ledgerx.event.TransactionCompletedEvent;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.TransactionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LedgerService ledgerService,
            AuditService auditService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransactionResponse transfer(
            String senderEmail,
            String senderAccountNumber,
            TransferRequest request
    ) {

        Transaction existingTransaction =
                transactionRepository
                        .findByIdempotencyKey(request.getIdempotencyKey())
                        .orElse(null);

        if (existingTransaction != null) {
            return new TransactionResponse(
                    existingTransaction.getId(),
                    existingTransaction.getTransactionReference(),
                    existingTransaction.getSenderAccount().getAccountNumber(),
                    existingTransaction.getReceiverAccount().getAccountNumber(),
                    existingTransaction.getAmount(),
                    existingTransaction.getStatus(),
                    existingTransaction.getCreatedAt()
            );
        }

        String receiverAccountNumber =
                request.getReceiverAccountNumber();

        if (senderAccountNumber.equals(receiverAccountNumber)) {
            throw new IllegalArgumentException(
                    "Sender and receiver accounts must be different"
            );
        }

        String firstAccountNumber;
        String secondAccountNumber;

        if (senderAccountNumber.compareTo(receiverAccountNumber) < 0) {
            firstAccountNumber = senderAccountNumber;
            secondAccountNumber = receiverAccountNumber;
        } else {
            firstAccountNumber = receiverAccountNumber;
            secondAccountNumber = senderAccountNumber;
        }

        Account firstAccount =
                accountRepository
                        .findByAccountNumberForUpdate(firstAccountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                ));

        Account secondAccount =
                accountRepository
                        .findByAccountNumberForUpdate(secondAccountNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                ));

        Account senderAccount;
        Account receiverAccount;

        if (firstAccount.getAccountNumber()
                .equals(senderAccountNumber)) {

            senderAccount = firstAccount;
            receiverAccount = secondAccount;

        } else {

            senderAccount = secondAccount;
            receiverAccount = firstAccount;
        }

        if (!senderAccount.getUser().getEmail()
                .equals(senderEmail)) {

            throw new IllegalArgumentException(
                    "You do not own this account"
            );
        }

        if (senderAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Sender account is not active"
            );
        }

        if (receiverAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Receiver account is not active"
            );
        }

        BigDecimal amount = request.getAmount();

        if (senderAccount.getBalance()
                .compareTo(amount) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient balance"
            );
        }

        senderAccount.setBalance(
                senderAccount.getBalance()
                        .subtract(amount)
        );

        receiverAccount.setBalance(
                receiverAccount.getBalance()
                        .add(amount)
        );

        String reference =
                "TXN-" + UUID.randomUUID();

        Transaction transaction =
                new Transaction();

        transaction.setTransactionReference(reference);
        transaction.setIdempotencyKey(
                request.getIdempotencyKey()
        );
        transaction.setSenderAccount(senderAccount);
        transaction.setReceiverAccount(receiverAccount);
        transaction.setAmount(amount);
        transaction.setStatus(
                TransactionStatus.SUCCESS
        );
        transaction.setCreatedAt(
                LocalDateTime.now()
        );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        ledgerService.recordTransfer(
                savedTransaction,
                senderAccount,
                receiverAccount,
                amount
        );

        auditService.log(
                senderEmail,
                "TRANSFER",
                "Transferred ₹" + amount
                        + " from "
                        + senderAccount.getAccountNumber()
                        + " to "
                        + receiverAccount.getAccountNumber()
        );

        eventPublisher.publishEvent(
                new TransactionCompletedEvent(
                        savedTransaction.getId(),
                        savedTransaction.getTransactionReference(),
                        senderEmail,
                        receiverAccount.getUser().getEmail(),
                        senderAccount.getAccountNumber(),
                        receiverAccount.getAccountNumber()
                )
        );

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getTransactionReference(),
                senderAccount.getAccountNumber(),
                receiverAccount.getAccountNumber(),
                savedTransaction.getAmount(),
                savedTransaction.getStatus(),
                savedTransaction.getCreatedAt()
        );
    }
}