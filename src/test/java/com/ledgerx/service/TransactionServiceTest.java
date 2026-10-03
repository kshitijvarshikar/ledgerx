package com.ledgerx.service;

import com.ledgerx.dto.TransactionResponse;
import com.ledgerx.dto.TransferRequest;
import com.ledgerx.entity.Account;
import com.ledgerx.entity.Transaction;
import com.ledgerx.entity.TransactionStatus;
import com.ledgerx.entity.User;
import com.ledgerx.event.TransactionCompletedEvent;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private AuditService auditService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldTransferMoneySuccessfully() {

        User senderUser = new User(
                "Sender",
                "sender@example.com",
                "password"
        );

        User receiverUser = new User(
                "Receiver",
                "receiver@example.com",
                "password"
        );

        Account senderAccount = new Account();
        senderAccount.setAccountNumber("1111111111");
        senderAccount.setBalance(new BigDecimal("5000.00"));
        senderAccount.setUser(senderUser);

        Account receiverAccount = new Account();
        receiverAccount.setAccountNumber("2222222222");
        receiverAccount.setBalance(new BigDecimal("1000.00"));
        receiverAccount.setUser(receiverUser);

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("1111111111");
        request.setReceiverAccountNumber("2222222222");
        request.setAmount(new BigDecimal("1000.00"));
        request.setIdempotencyKey("test-idempotency-key");

        when(transactionRepository.findByIdempotencyKey(
                "test-idempotency-key"
        )).thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate(
                "1111111111"
        )).thenReturn(Optional.of(senderAccount));

        when(accountRepository.findByAccountNumberForUpdate(
                "2222222222"
        )).thenReturn(Optional.of(receiverAccount));

        Transaction savedTransaction = new Transaction();

        savedTransaction.setTransactionReference("TXN-TEST-123");
        savedTransaction.setIdempotencyKey("test-idempotency-key");
        savedTransaction.setSenderAccount(senderAccount);
        savedTransaction.setReceiverAccount(receiverAccount);
        savedTransaction.setAmount(new BigDecimal("1000.00"));
        savedTransaction.setStatus(TransactionStatus.SUCCESS);
        savedTransaction.setCreatedAt(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        TransactionResponse response = transactionService.transfer(
                "sender@example.com",
                "1111111111",
                request
        );

        assertNotNull(response);

        assertEquals(
                new BigDecimal("4000.00"),
                senderAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("2000.00"),
                receiverAccount.getBalance()
        );

        verify(transactionRepository)
                .save(any(Transaction.class));

        verify(ledgerService)
                .recordTransfer(
                        any(Transaction.class),
                        eq(senderAccount),
                        eq(receiverAccount),
                        eq(new BigDecimal("1000.00"))
                );

        verify(auditService)
                .log(
                        eq("sender@example.com"),
                        eq("TRANSFER"),
                        anyString()
                );

        verify(eventPublisher)
                .publishEvent(
                        any(TransactionCompletedEvent.class)
                );
    }

    @Test
    void shouldRejectTransferWhenBalanceIsInsufficient() {

        User senderUser = new User(
                "Sender",
                "sender@example.com",
                "password"
        );

        Account senderAccount = new Account();
        senderAccount.setAccountNumber("1111111111");
        senderAccount.setBalance(new BigDecimal("500.00"));
        senderAccount.setUser(senderUser);

        Account receiverAccount = new Account();
        receiverAccount.setAccountNumber("2222222222");
        receiverAccount.setBalance(new BigDecimal("1000.00"));

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("1111111111");
        request.setReceiverAccountNumber("2222222222");
        request.setAmount(new BigDecimal("1000.00"));
        request.setIdempotencyKey("insufficient-balance-key");

        when(transactionRepository.findByIdempotencyKey(
                "insufficient-balance-key"
        )).thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate(
                "1111111111"
        )).thenReturn(Optional.of(senderAccount));

        when(accountRepository.findByAccountNumberForUpdate(
                "2222222222"
        )).thenReturn(Optional.of(receiverAccount));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(
                        "sender@example.com",
                        "1111111111",
                        request
                )
        );

        assertEquals(
                new BigDecimal("500.00"),
                senderAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                receiverAccount.getBalance()
        );
    }

    @Test
    void shouldReturnExistingTransactionForSameIdempotencyKey() {

        User senderUser = new User(
                "Sender",
                "sender@example.com",
                "password"
        );

        Account senderAccount = new Account();
        senderAccount.setAccountNumber("1111111111");
        senderAccount.setBalance(new BigDecimal("5000.00"));
        senderAccount.setUser(senderUser);

        Account receiverAccount = new Account();
        receiverAccount.setAccountNumber("2222222222");
        receiverAccount.setBalance(new BigDecimal("1000.00"));

        Transaction existingTransaction = new Transaction();
        existingTransaction.setTransactionReference("TXN-EXISTING");
        existingTransaction.setIdempotencyKey("same-key");
        existingTransaction.setSenderAccount(senderAccount);
        existingTransaction.setReceiverAccount(receiverAccount);
        existingTransaction.setAmount(new BigDecimal("1000.00"));
        existingTransaction.setStatus(TransactionStatus.SUCCESS);
        existingTransaction.setCreatedAt(LocalDateTime.now());

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("1111111111");
        request.setReceiverAccountNumber("2222222222");
        request.setAmount(new BigDecimal("1000.00"));
        request.setIdempotencyKey("same-key");

        when(transactionRepository.findByIdempotencyKey("same-key"))
                .thenReturn(Optional.of(existingTransaction));

        TransactionResponse response = transactionService.transfer(
                "sender@example.com",
                "1111111111",
                request
        );

        assertNotNull(response);

        assertEquals(
                "TXN-EXISTING",
                response.getTransactionReference()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                response.getAmount()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                response.getStatus()
        );

        verify(transactionRepository)
                .findByIdempotencyKey("same-key");

        verify(transactionRepository, org.mockito.Mockito.never())
                .save(any(Transaction.class));

        verify(ledgerService, org.mockito.Mockito.never())
                .recordTransfer(
                        any(Transaction.class),
                        any(Account.class),
                        any(Account.class),
                        any(BigDecimal.class)
                );
    }

    @Test
    void shouldRejectTransferWhenSenderDoesNotOwnAccount() {

        User actualOwner = new User(
                "Account Owner",
                "owner@example.com",
                "password"
        );

        Account senderAccount = new Account();
        senderAccount.setAccountNumber("1111111111");
        senderAccount.setBalance(new BigDecimal("5000.00"));
        senderAccount.setUser(actualOwner);

        Account receiverAccount = new Account();
        receiverAccount.setAccountNumber("2222222222");
        receiverAccount.setBalance(new BigDecimal("1000.00"));

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("1111111111");
        request.setReceiverAccountNumber("2222222222");
        request.setAmount(new BigDecimal("1000.00"));
        request.setIdempotencyKey("ownership-test-key");

        when(transactionRepository.findByIdempotencyKey(
                "ownership-test-key"
        )).thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate(
                "1111111111"
        )).thenReturn(Optional.of(senderAccount));

        when(accountRepository.findByAccountNumberForUpdate(
                "2222222222"
        )).thenReturn(Optional.of(receiverAccount));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(
                        "attacker@example.com",
                        "1111111111",
                        request
                )
        );

        assertEquals(
                new BigDecimal("5000.00"),
                senderAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                receiverAccount.getBalance()
        );

        verify(transactionRepository, org.mockito.Mockito.never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldRejectTransferWhenSenderAccountIsNotActive() {

        User senderUser = new User(
                "Sender",
                "sender@example.com",
                "password"
        );

        Account senderAccount = new Account();
        senderAccount.setAccountNumber("1111111111");
        senderAccount.setBalance(new BigDecimal("5000.00"));
        senderAccount.setStatus(
                com.ledgerx.entity.AccountStatus.BLOCKED
        );
        senderAccount.setUser(senderUser);

        Account receiverAccount = new Account();
        receiverAccount.setAccountNumber("2222222222");
        receiverAccount.setBalance(new BigDecimal("1000.00"));

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("1111111111");
        request.setReceiverAccountNumber("2222222222");
        request.setAmount(new BigDecimal("1000.00"));
        request.setIdempotencyKey("blocked-account-key");

        when(transactionRepository.findByIdempotencyKey(
                "blocked-account-key"
        )).thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate(
                "1111111111"
        )).thenReturn(Optional.of(senderAccount));

        when(accountRepository.findByAccountNumberForUpdate(
                "2222222222"
        )).thenReturn(Optional.of(receiverAccount));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(
                        "sender@example.com",
                        "1111111111",
                        request
                )
        );

        assertEquals(
                new BigDecimal("5000.00"),
                senderAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                receiverAccount.getBalance()
        );

        verify(transactionRepository, org.mockito.Mockito.never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldRejectTransferWhenReceiverAccountIsNotActive() {

        User senderUser = new User(
                "Sender",
                "sender@example.com",
                "password"
        );

        User receiverUser = new User(
                "Receiver",
                "receiver@example.com",
                "password"
        );

        Account senderAccount = new Account();
        senderAccount.setAccountNumber("1111111111");
        senderAccount.setBalance(new BigDecimal("5000.00"));
        senderAccount.setUser(senderUser);

        Account receiverAccount = new Account();
        receiverAccount.setAccountNumber("2222222222");
        receiverAccount.setBalance(new BigDecimal("1000.00"));
        receiverAccount.setStatus(
                com.ledgerx.entity.AccountStatus.BLOCKED
        );
        receiverAccount.setUser(receiverUser);

        TransferRequest request = new TransferRequest();
        request.setSenderAccountNumber("1111111111");
        request.setReceiverAccountNumber("2222222222");
        request.setAmount(new BigDecimal("1000.00"));
        request.setIdempotencyKey("receiver-blocked-key");

        when(transactionRepository.findByIdempotencyKey(
                "receiver-blocked-key"
        )).thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberForUpdate(
                "1111111111"
        )).thenReturn(Optional.of(senderAccount));

        when(accountRepository.findByAccountNumberForUpdate(
                "2222222222"
        )).thenReturn(Optional.of(receiverAccount));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(
                        "sender@example.com",
                        "1111111111",
                        request
                )
        );

        assertEquals(
                new BigDecimal("5000.00"),
                senderAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                receiverAccount.getBalance()
        );

        verify(transactionRepository, org.mockito.Mockito.never())
                .save(any(Transaction.class));
    }
}