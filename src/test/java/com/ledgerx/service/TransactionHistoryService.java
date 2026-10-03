package com.ledgerx.service;

import com.ledgerx.dto.TransactionResponse;
import com.ledgerx.entity.Account;
import com.ledgerx.entity.Transaction;
import com.ledgerx.entity.TransactionStatus;
import com.ledgerx.entity.User;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.TransactionRepository;
import com.ledgerx.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionHistoryService transactionHistoryService;

    @Test
    void shouldReturnTransactionsFromAllUserAccounts() {

        User user = createUser();

        Account accountOne = createAccount(
                "1111111111",
                1L,
                user
        );

        Account accountTwo = createAccount(
                "2222222222",
                2L,
                user
        );

        Transaction transaction = createTransaction(
                accountOne,
                accountTwo,
                "TXN-TEST-1",
                new BigDecimal("500.00"),
                TransactionStatus.SUCCESS
        );

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction));

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(user.getId()))
                .thenReturn(List.of(accountOne, accountTwo));

        when(transactionRepository.findByAccountIds(
                eq(List.of(1L, 2L)),
                any(PageRequest.class)
        )).thenReturn(page);

        Page<TransactionResponse> result =
                transactionHistoryService.getTransactionHistory(
                        "user@example.com",
                        0,
                        10,
                        null,
                        null,
                        null
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(
                "TXN-TEST-1",
                result.getContent()
                        .get(0)
                        .getTransactionReference()
        );

        verify(transactionRepository)
                .findByAccountIds(
                        eq(List.of(1L, 2L)),
                        any(PageRequest.class)
                );
    }

    @Test
    void shouldReturnEmptyPageWhenUserHasNoAccounts() {

        User user = createUser();

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(user.getId()))
                .thenReturn(List.of());

        Page<TransactionResponse> result =
                transactionHistoryService.getTransactionHistory(
                        "user@example.com",
                        0,
                        10,
                        null,
                        null,
                        null
                );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void shouldFilterTransactionsByStatus() {

        User user = createUser();

        Account accountOne = createAccount(
                "1111111111",
                1L,
                user
        );

        Account accountTwo = createAccount(
                "2222222222",
                2L,
                user
        );

        Transaction transaction = createTransaction(
                accountOne,
                accountTwo,
                "TXN-SUCCESS-1",
                new BigDecimal("1000.00"),
                TransactionStatus.SUCCESS
        );

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction));

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(user.getId()))
                .thenReturn(List.of(accountOne, accountTwo));

        when(transactionRepository.findByAccountIdsAndStatus(
                eq(List.of(1L, 2L)),
                eq(TransactionStatus.SUCCESS),
                any(PageRequest.class)
        )).thenReturn(page);

        Page<TransactionResponse> result =
                transactionHistoryService.getTransactionHistory(
                        "user@example.com",
                        0,
                        10,
                        TransactionStatus.SUCCESS,
                        null,
                        null
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(
                TransactionStatus.SUCCESS,
                result.getContent()
                        .get(0)
                        .getStatus()
        );

        verify(transactionRepository)
                .findByAccountIdsAndStatus(
                        eq(List.of(1L, 2L)),
                        eq(TransactionStatus.SUCCESS),
                        any(PageRequest.class)
                );
    }

    @Test
    void shouldFilterTransactionsByDateRange() {

        User user = createUser();

        Account accountOne = createAccount(
                "1111111111",
                1L,
                user
        );

        Account accountTwo = createAccount(
                "2222222222",
                2L,
                user
        );

        LocalDateTime from =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        LocalDateTime to =
                LocalDateTime.of(2026, 10, 2, 23, 59);

        Transaction transaction = createTransaction(
                accountOne,
                accountTwo,
                "TXN-DATE-1",
                new BigDecimal("750.00"),
                TransactionStatus.SUCCESS
        );

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction));

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(user.getId()))
                .thenReturn(List.of(accountOne, accountTwo));

        when(transactionRepository
                .findByAccountIdsAndCreatedAtBetween(
                        eq(List.of(1L, 2L)),
                        eq(from),
                        eq(to),
                        any(PageRequest.class)
                )).thenReturn(page);

        Page<TransactionResponse> result =
                transactionHistoryService.getTransactionHistory(
                        "user@example.com",
                        0,
                        10,
                        null,
                        from,
                        to
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(
                "TXN-DATE-1",
                result.getContent()
                        .get(0)
                        .getTransactionReference()
        );

        verify(transactionRepository)
                .findByAccountIdsAndCreatedAtBetween(
                        eq(List.of(1L, 2L)),
                        eq(from),
                        eq(to),
                        any(PageRequest.class)
                );
    }

    @Test
    void shouldFilterTransactionsByStatusAndDateRange() {

        User user = createUser();

        Account accountOne = createAccount(
                "1111111111",
                1L,
                user
        );

        Account accountTwo = createAccount(
                "2222222222",
                2L,
                user
        );

        LocalDateTime from =
                LocalDateTime.of(2026, 10, 1, 0, 0);

        LocalDateTime to =
                LocalDateTime.of(2026, 10, 2, 23, 59);

        Transaction transaction = createTransaction(
                accountOne,
                accountTwo,
                "TXN-FILTER-1",
                new BigDecimal("900.00"),
                TransactionStatus.SUCCESS
        );

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction));

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(user.getId()))
                .thenReturn(List.of(accountOne, accountTwo));

        when(transactionRepository
                .findByAccountIdsAndStatusAndCreatedAtBetween(
                        eq(List.of(1L, 2L)),
                        eq(TransactionStatus.SUCCESS),
                        eq(from),
                        eq(to),
                        any(PageRequest.class)
                )).thenReturn(page);

        Page<TransactionResponse> result =
                transactionHistoryService.getTransactionHistory(
                        "user@example.com",
                        0,
                        10,
                        TransactionStatus.SUCCESS,
                        from,
                        to
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(
                TransactionStatus.SUCCESS,
                result.getContent()
                        .get(0)
                        .getStatus()
        );

        verify(transactionRepository)
                .findByAccountIdsAndStatusAndCreatedAtBetween(
                        eq(List.of(1L, 2L)),
                        eq(TransactionStatus.SUCCESS),
                        eq(from),
                        eq(to),
                        any(PageRequest.class)
                );
    }

    @Test
    void shouldApplyPagination() {

        User user = createUser();

        Account account = createAccount(
                "1111111111",
                1L,
                user
        );

        Transaction transactionOne = createTransaction(
                account,
                account,
                "TXN-PAGE-1",
                new BigDecimal("100.00"),
                TransactionStatus.SUCCESS
        );

        Transaction transactionTwo = createTransaction(
                account,
                account,
                "TXN-PAGE-2",
                new BigDecimal("200.00"),
                TransactionStatus.SUCCESS
        );

        Page<Transaction> page =
                new PageImpl<>(
                        List.of(transactionOne, transactionTwo),
                        PageRequest.of(1, 2),
                        5
                );

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(user.getId()))
                .thenReturn(List.of(account));

        when(transactionRepository.findByAccountIds(
                eq(List.of(1L)),
                any(PageRequest.class)
        )).thenReturn(page);

        Page<TransactionResponse> result =
                transactionHistoryService.getTransactionHistory(
                        "user@example.com",
                        1,
                        2,
                        null,
                        null,
                        null
                );

        assertNotNull(result);
        assertEquals(5, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalPages());

        verify(transactionRepository)
                .findByAccountIds(
                        eq(List.of(1L)),
                        any(PageRequest.class)
                );
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        transactionHistoryService
                                .getTransactionHistory(
                                        "unknown@example.com",
                                        0,
                                        10,
                                        null,
                                        null,
                                        null
                                )
        );
    }

    private User createUser() {

        User user = new User(
                "Test User",
                "user@example.com",
                "password"
        );

        return user;
    }

    private Account createAccount(
            String accountNumber,
            Long id,
            User user
    ) {

        Account account =
                org.mockito.Mockito.spy(new Account());

        account.setAccountNumber(accountNumber);
        account.setUser(user);

        doReturn(id)
                .when(account)
                .getId();

        return account;
    }

    private Transaction createTransaction(
            Account sender,
            Account receiver,
            String reference,
            BigDecimal amount,
            TransactionStatus status
    ) {

        Transaction transaction =
                new Transaction();

        transaction.setTransactionReference(reference);
        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);
        transaction.setAmount(amount);
        transaction.setStatus(status);
        transaction.setCreatedAt(
                LocalDateTime.of(
                        2026,
                        10,
                        1,
                        10,
                        30
                )
        );

        return transaction;
    }
}