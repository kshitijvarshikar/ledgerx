package com.ledgerx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BeneficiaryRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Nickname is required")
    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    private String nickname;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}