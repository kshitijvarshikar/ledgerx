package com.ledgerx.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user_email", columnList = "user_email")
        }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String userEmail;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public Long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}