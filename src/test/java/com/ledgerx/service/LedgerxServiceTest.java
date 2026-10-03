package com.ledgerx.service;

import com.ledgerx.entity.Account;
import com.ledgerx.entity.LedgerEntry;
import com.ledgerx.entity.LedgerEntryType;
import com.ledgerx.entity.Transaction;
import com.ledgerx.repository.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void shouldCreateDebitAndCreditEntries() {

        Transaction transaction = new Transaction();

        Account senderAccount = new Account();
        senderAccount.setAccountNumber("1234567890");

        Account receiverAccount = new Account();
        receiverAccount.setAccountNumber("9876543210");

        BigDecimal amount = new BigDecimal("500.00");

        ledgerService.recordTransfer(
                transaction,
                senderAccount,
                receiverAccount,
                amount
        );

        ArgumentCaptor<LedgerEntry> captor =
                ArgumentCaptor.forClass(LedgerEntry.class);

        verify(ledgerEntryRepository, times(2))
                .save(captor.capture());

        var entries = captor.getAllValues();

        assertEquals(2, entries.size());

        LedgerEntry debitEntry = entries.get(0);
        LedgerEntry creditEntry = entries.get(1);

        assertEquals(LedgerEntryType.DEBIT,
                debitEntry.getEntryType());

        assertEquals(senderAccount,
                debitEntry.getAccount());

        assertEquals(transaction,
                debitEntry.getTransaction());

        assertEquals(amount,
                debitEntry.getAmount());

        assertEquals(LedgerEntryType.CREDIT,
                creditEntry.getEntryType());

        assertEquals(receiverAccount,
                creditEntry.getAccount());

        assertEquals(transaction,
                creditEntry.getTransaction());

        assertEquals(amount,
                creditEntry.getAmount());

        assertNotNull(debitEntry.getCreatedAt());
        assertNotNull(creditEntry.getCreatedAt());
    }

    @Test
    void shouldSaveDebitEntryBeforeCreditEntry() {

        Transaction transaction = new Transaction();

        Account senderAccount = new Account();
        Account receiverAccount = new Account();

        BigDecimal amount = new BigDecimal("1000.00");

        ledgerService.recordTransfer(
                transaction,
                senderAccount,
                receiverAccount,
                amount
        );

        ArgumentCaptor<LedgerEntry> captor =
                ArgumentCaptor.forClass(LedgerEntry.class);

        verify(ledgerEntryRepository, times(2))
                .save(captor.capture());

        var entries = captor.getAllValues();

        assertEquals(
                LedgerEntryType.DEBIT,
                entries.get(0).getEntryType()
        );

        assertEquals(
                LedgerEntryType.CREDIT,
                entries.get(1).getEntryType()
        );
    }

    @Test
    void shouldUseSameAmountForBothLedgerEntries() {

        Transaction transaction = new Transaction();

        Account senderAccount = new Account();
        Account receiverAccount = new Account();

        BigDecimal amount = new BigDecimal("2500.75");

        ledgerService.recordTransfer(
                transaction,
                senderAccount,
                receiverAccount,
                amount
        );

        ArgumentCaptor<LedgerEntry> captor =
                ArgumentCaptor.forClass(LedgerEntry.class);

        verify(ledgerEntryRepository, times(2))
                .save(captor.capture());

        var entries = captor.getAllValues();

        assertEquals(amount, entries.get(0).getAmount());
        assertEquals(amount, entries.get(1).getAmount());
    }
}