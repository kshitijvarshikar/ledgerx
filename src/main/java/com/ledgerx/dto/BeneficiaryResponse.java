package com.ledgerx.dto;

import java.time.LocalDateTime;

public class BeneficiaryResponse {

    private Long id;
    private String accountNumber;
    private String nickname;
    private LocalDateTime createdAt;

    public BeneficiaryResponse() {
    }

    public BeneficiaryResponse(
            Long id,
            String accountNumber,
            String nickname,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}