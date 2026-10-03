package com.ledgerx.dto;

import com.ledgerx.entity.AccountStatus;

import java.math.BigDecimal;

public class AccountResponse {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;

    public AccountResponse() {
    }

    public AccountResponse(
            Long id,
            String accountNumber,
            BigDecimal balance,
            String currency,
            AccountStatus status
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }
}