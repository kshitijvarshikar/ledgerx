package com.ledgerx.controller;

import com.ledgerx.dto.TransactionResponse;
import com.ledgerx.dto.TransferRequest;
import com.ledgerx.entity.TransactionStatus;
import com.ledgerx.service.TransactionHistoryService;
import com.ledgerx.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionHistoryService transactionHistoryService;

    public TransactionController(
            TransactionService transactionService,
            TransactionHistoryService transactionHistoryService
    ) {
        this.transactionService = transactionService;
        this.transactionHistoryService = transactionHistoryService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request
    ) {

        String email = authentication.getName();

        TransactionResponse response =
                transactionService.transfer(
                        email,
                        request.getSenderAccountNumber(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        if ((from != null && to == null) ||
                (from == null && to != null)) {

            throw new IllegalArgumentException(
                    "Both from and to dates are required"
            );
        }

        if (from != null && to != null &&
                from.isAfter(to)) {

            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }

        String email = authentication.getName();

        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (from != null) {
            fromDateTime = from.atStartOfDay();
        }

        if (to != null) {
            toDateTime = to.atTime(23, 59, 59, 999999999);
        }

        Page<TransactionResponse> response =
                transactionHistoryService.getTransactionHistory(
                        email,
                        page,
                        size,
                        status,
                        fromDateTime,
                        toDateTime
                );

        return ResponseEntity.ok(response);
    }
}