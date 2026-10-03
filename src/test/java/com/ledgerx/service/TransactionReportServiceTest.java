package com.ledgerx.service;

import com.ledgerx.dto.TransactionSummaryResponse;
import com.ledgerx.entity.Account;
import com.ledgerx.entity.User;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.TransactionReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionReportServiceTest {

    @Mock
    private TransactionReportRepository transactionReportRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionReportService transactionReportService;

    @Test
    void shouldGetTotalTransferredAmount() {

        User user = new User(
                "John Doe",
                "john@example.com",
                "password"
        );

        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setUser(user);

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(account));

        when(transactionReportRepository
                .getTotalTransferredAmount("1234567890"))
                .thenReturn(new BigDecimal("5000.00"));

        BigDecimal result =
                transactionReportService.getTotalTransferredAmount(
                        "john@example.com",
                        "1234567890"
                );

        assertEquals(
                new BigDecimal("5000.00"),
                result
        );

        verify(transactionReportRepository)
                .getTotalTransferredAmount("1234567890");
    }

    @Test
    void shouldRejectUnknownAccount() {

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionReportService
                        .getTotalTransferredAmount(
                                "john@example.com",
                                "1234567890"
                        )
        );

        assertEquals(
                "Account not found",
                exception.getMessage()
        );

        verify(
                transactionReportRepository,
                never()
        ).getTotalTransferredAmount(anyString());
    }

    @Test
    void shouldRejectAccountOwnedByAnotherUser() {

        User user = new User(
                "John Doe",
                "john@example.com",
                "password"
        );

        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setUser(user);

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionReportService
                        .getTotalTransferredAmount(
                                "other@example.com",
                                "1234567890"
                        )
        );

        assertEquals(
                "You do not own this account",
                exception.getMessage()
        );

        verify(
                transactionReportRepository,
                never()
        ).getTotalTransferredAmount(anyString());
    }

    @Test
    void shouldGetTransactionSummary() {

        User user = new User(
                "John Doe",
                "john@example.com",
                "password"
        );

        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setUser(user);

        TransactionSummaryResponse summary =
                new TransactionSummaryResponse(
                        10L,
                        new BigDecimal("5000.00"),
                        8L,
                        2L
                );

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(account));

        when(transactionReportRepository
                .getTransactionSummary("1234567890"))
                .thenReturn(summary);

        TransactionSummaryResponse result =
                transactionReportService.getTransactionSummary(
                        "john@example.com",
                        "1234567890"
                );

        assertNotNull(result);
        assertEquals(10L, result.getTotalTransactions());
        assertEquals(
                new BigDecimal("5000.00"),
                result.getTotalAmountTransferred()
        );
        assertEquals(8L, result.getSuccessfulTransactions());
        assertEquals(2L, result.getFailedTransactions());

        verify(transactionReportRepository)
                .getTransactionSummary("1234567890");
    }

    @Test
    void shouldGetTransactionSummaryByDateRange() {

        User user = new User(
                "John Doe",
                "john@example.com",
                "password"
        );

        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setUser(user);

        LocalDateTime from =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        LocalDateTime to =
                LocalDateTime.of(2026, 1, 31, 23, 59);

        TransactionSummaryResponse summary =
                new TransactionSummaryResponse(
                        5L,
                        new BigDecimal("2500.00"),
                        5L,
                        0L
                );

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(account));

        when(transactionReportRepository
                .getTransactionSummaryByDateRange(
                        "1234567890",
                        from,
                        to
                ))
                .thenReturn(summary);

        TransactionSummaryResponse result =
                transactionReportService
                        .getTransactionSummaryByDateRange(
                                "john@example.com",
                                "1234567890",
                                from,
                                to
                        );

        assertNotNull(result);
        assertEquals(5L, result.getTotalTransactions());
        assertEquals(
                new BigDecimal("2500.00"),
                result.getTotalAmountTransferred()
        );
        assertEquals(5L, result.getSuccessfulTransactions());
        assertEquals(0L, result.getFailedTransactions());

        verify(transactionReportRepository)
                .getTransactionSummaryByDateRange(
                        "1234567890",
                        from,
                        to
                );
    }
}