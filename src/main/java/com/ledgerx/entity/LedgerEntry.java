package com.ledgerx.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_account", columnList = "account_id"),
                @Index(name = "idx_ledger_transaction", columnList = "transaction_id")
        }
)
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryType entryType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public LedgerEntry() {
    }

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Account getAccount() {
        return account;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setEntryType(LedgerEntryType entryType) {
        this.entryType = entryType;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}