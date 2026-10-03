package com.ledgerx.controller;

import com.ledgerx.dto.TransactionResponse;
import com.ledgerx.dto.TransferRequest;
import com.ledgerx.entity.TransactionStatus;
import com.ledgerx.service.TransactionHistoryService;
import com.ledgerx.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionHistoryService transactionHistoryService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {

        transactionResponse =
                new TransactionResponse(
                        1L,
                        "TXN-12345",
                        "7067894288",
                        "7411250485",
                        new BigDecimal("500.00"),
                        TransactionStatus.SUCCESS,
                        LocalDateTime.now()
                );
    }

    @Test
    void shouldTransferSuccessfully() {

        TransferRequest request =
                new TransferRequest();

        request.setSenderAccountNumber("7067894288");
        request.setReceiverAccountNumber("7411250485");
        request.setAmount(new BigDecimal("500.00"));
        request.setIdempotencyKey("test-key-123");

        when(authentication.getName())
                .thenReturn("user@example.com");

        when(transactionService.transfer(
                eq("user@example.com"),
                eq("7067894288"),
                same(request)
        )).thenReturn(transactionResponse);

        var response =
                transactionController.transfer(
                        authentication,
                        request
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(
                "TXN-12345",
                response.getBody().getTransactionReference()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                response.getBody().getStatus()
        );

        verify(transactionService)
                .transfer(
                        "user@example.com",
                        "7067894288",
                        request
                );
    }

    @Test
    void shouldGetTransactionHistorySuccessfully() {

        Page<TransactionResponse> page =
                new PageImpl<>(
                        List.of(transactionResponse),
                        PageRequest.of(0, 10),
                        1
                );

        when(authentication.getName())
                .thenReturn("user@example.com");

        when(transactionHistoryService.getTransactionHistory(
                eq("user@example.com"),
                eq(0),
                eq(10),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(page);

        var response =
                transactionController.getTransactionHistory(
                        authentication,
                        0,
                        10,
                        null,
                        null,
                        null
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getContent().size());

        verify(transactionHistoryService)
                .getTransactionHistory(
                        "user@example.com",
                        0,
                        10,
                        null,
                        null,
                        null
                );
    }

    @Test
    void shouldGetHistoryWithStatusFilter() {

        Page<TransactionResponse> page =
                new PageImpl<>(
                        List.of(transactionResponse)
                );

        when(authentication.getName())
                .thenReturn("user@example.com");

        when(transactionHistoryService.getTransactionHistory(
                eq("user@example.com"),
                eq(0),
                eq(10),
                eq(TransactionStatus.SUCCESS),
                isNull(),
                isNull()
        )).thenReturn(page);

        var response =
                transactionController.getTransactionHistory(
                        authentication,
                        0,
                        10,
                        TransactionStatus.SUCCESS,
                        null,
                        null
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(transactionHistoryService)
                .getTransactionHistory(
                        "user@example.com",
                        0,
                        10,
                        TransactionStatus.SUCCESS,
                        null,
                        null
                );
    }

    @Test
    void shouldGetHistoryWithDateRange() {

        LocalDate from =
                LocalDate.of(2026, 10, 1);

        LocalDate to =
                LocalDate.of(2026, 10, 5);

        Page<TransactionResponse> page =
                new PageImpl<>(
                        List.of(transactionResponse)
                );

        when(authentication.getName())
                .thenReturn("user@example.com");

        when(transactionHistoryService.getTransactionHistory(
                eq("user@example.com"),
                eq(0),
                eq(10),
                isNull(),
                any(),
                any()
        )).thenReturn(page);

        var response =
                transactionController.getTransactionHistory(
                        authentication,
                        0,
                        10,
                        null,
                        from,
                        to
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(transactionHistoryService)
                .getTransactionHistory(
                        eq("user@example.com"),
                        eq(0),
                        eq(10),
                        isNull(),
                        eq(from.atStartOfDay()),
                        eq(to.atTime(23, 59, 59, 999999999))
                );
    }

    @Test
    void shouldGetHistoryWithStatusAndDateRange() {

        LocalDate from =
                LocalDate.of(2026, 10, 1);

        LocalDate to =
                LocalDate.of(2026, 10, 5);

        Page<TransactionResponse> page =
                new PageImpl<>(
                        List.of(transactionResponse)
                );

        when(authentication.getName())
                .thenReturn("user@example.com");

        when(transactionHistoryService.getTransactionHistory(
                eq("user@example.com"),
                eq(1),
                eq(5),
                eq(TransactionStatus.SUCCESS),
                any(),
                any()
        )).thenReturn(page);

        var response =
                transactionController.getTransactionHistory(
                        authentication,
                        1,
                        5,
                        TransactionStatus.SUCCESS,
                        from,
                        to
                );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(transactionHistoryService)
                .getTransactionHistory(
                        eq("user@example.com"),
                        eq(1),
                        eq(5),
                        eq(TransactionStatus.SUCCESS),
                        eq(from.atStartOfDay()),
                        eq(to.atTime(23, 59, 59, 999999999))
                );
    }

    @Test
    void shouldRejectWhenOnlyFromDateIsProvided() {

        LocalDate from =
                LocalDate.of(2026, 10, 1);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionController
                                .getTransactionHistory(
                                        authentication,
                                        0,
                                        10,
                                        null,
                                        from,
                                        null
                                )
                );

        assertEquals(
                "Both from and to dates are required",
                exception.getMessage()
        );

        verifyNoInteractions(transactionHistoryService);
    }

    @Test
    void shouldRejectWhenOnlyToDateIsProvided() {

        LocalDate to =
                LocalDate.of(2026, 10, 5);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionController
                                .getTransactionHistory(
                                        authentication,
                                        0,
                                        10,
                                        null,
                                        null,
                                        to
                                )
                );

        assertEquals(
                "Both from and to dates are required",
                exception.getMessage()
        );

        verifyNoInteractions(transactionHistoryService);
    }

    @Test
    void shouldRejectWhenFromDateIsAfterToDate() {

        LocalDate from =
                LocalDate.of(2026, 10, 10);

        LocalDate to =
                LocalDate.of(2026, 10, 5);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionController
                                .getTransactionHistory(
                                        authentication,
                                        0,
                                        10,
                                        null,
                                        from,
                                        to
                                )
                );

        assertEquals(
                "From date cannot be after to date",
                exception.getMessage()
        );

        verifyNoInteractions(transactionHistoryService);
    }
}