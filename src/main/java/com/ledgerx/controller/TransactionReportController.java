package com.ledgerx.controller;

import com.ledgerx.dto.TransactionSummaryResponse;
import com.ledgerx.service.TransactionReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class TransactionReportController {

    private final TransactionReportService transactionReportService;

    public TransactionReportController(
            TransactionReportService transactionReportService
    ) {
        this.transactionReportService =
                transactionReportService;
    }

    @GetMapping("/total-transferred/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getTotalTransferredAmount(
            @PathVariable String accountNumber,
            Authentication authentication
    ) {

        String email = authentication.getName();

        BigDecimal total =
                transactionReportService
                        .getTotalTransferredAmount(
                                email,
                                accountNumber
                        );

        return ResponseEntity.ok(
                Map.of(
                        "accountNumber", accountNumber,
                        "totalTransferredAmount", total
                )
        );
    }

    @GetMapping("/summary/{accountNumber}")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @PathVariable String accountNumber,
            Authentication authentication
    ) {

        String email = authentication.getName();

        TransactionSummaryResponse response =
                transactionReportService
                        .getTransactionSummary(
                                email,
                                accountNumber
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{accountNumber}/date-range")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummaryByDateRange(
            @PathVariable String accountNumber,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            Authentication authentication
    ) {

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }

        String email = authentication.getName();

        LocalDateTime fromDateTime =
                from.atStartOfDay();

        LocalDateTime toDateTime =
                to.atTime(23, 59, 59, 999999999);

        TransactionSummaryResponse response =
                transactionReportService
                        .getTransactionSummaryByDateRange(
                                email,
                                accountNumber,
                                fromDateTime,
                                toDateTime
                        );

        return ResponseEntity.ok(response);
    }
}