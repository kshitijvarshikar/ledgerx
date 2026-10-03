package com.ledgerx.dto;

public class LoginResponse {

    private String token;
    private String tokenType;

    public LoginResponse() {
    }

    public LoginResponse(String token, String tokenType) {
        this.token = token;
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }
}