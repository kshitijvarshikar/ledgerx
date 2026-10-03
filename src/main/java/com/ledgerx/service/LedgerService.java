package com.ledgerx.service;

import com.ledgerx.entity.Account;
import com.ledgerx.entity.LedgerEntry;
import com.ledgerx.entity.LedgerEntryType;
import com.ledgerx.entity.Transaction;
import com.ledgerx.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public void recordTransfer(
            Transaction transaction,
            Account senderAccount,
            Account receiverAccount,
            BigDecimal amount
    ) {

        // Debit entry for sender
        LedgerEntry debitEntry = new LedgerEntry();

        debitEntry.setTransaction(transaction);
        debitEntry.setAccount(senderAccount);
        debitEntry.setEntryType(LedgerEntryType.DEBIT);
        debitEntry.setAmount(amount);
        debitEntry.setCreatedAt(LocalDateTime.now());

        ledgerEntryRepository.save(debitEntry);

        // Credit entry for receiver
        LedgerEntry creditEntry = new LedgerEntry();

        creditEntry.setTransaction(transaction);
        creditEntry.setAccount(receiverAccount);
        creditEntry.setEntryType(LedgerEntryType.CREDIT);
        creditEntry.setAmount(amount);
        creditEntry.setCreatedAt(LocalDateTime.now());

        ledgerEntryRepository.save(creditEntry);
    }
}