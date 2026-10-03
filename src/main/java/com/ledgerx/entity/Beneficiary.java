package com.ledgerx.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "beneficiaries",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "account_number"}
                )
        },
        indexes = {
                @Index(name = "idx_beneficiary_user", columnList = "user_id")
        }
)
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Beneficiary() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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

    public void setUser(User user) {
        this.user = user;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}