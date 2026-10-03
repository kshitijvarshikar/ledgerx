package com.ledgerx.repository;

import com.ledgerx.dto.TransactionSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.math.BigDecimal;

@Repository
public class TransactionReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BigDecimal getTotalTransferredAmount(
            String accountNumber
    ) {

        String sql = """
                SELECT COALESCE(SUM(t.amount), 0)
                FROM transactions t
                JOIN accounts a
                    ON t.sender_account_id = a.id
                WHERE a.account_number = ?
                AND t.status = 'SUCCESS'
                """;

        return jdbcTemplate.queryForObject(
                sql,
                BigDecimal.class,
                accountNumber
        );
    }

    public TransactionSummaryResponse getTransactionSummary(
            String accountNumber
    ) {

        String sql = """
                SELECT
                    COUNT(*) AS total_transactions,
                
                    COALESCE(
                        SUM(
                            CASE
                                WHEN t.status = 'SUCCESS'
                                THEN t.amount
                                ELSE 0
                            END
                        ),
                        0
                    ) AS total_amount,
                
                    SUM(CASE
                        WHEN t.status = 'SUCCESS' THEN 1
                        ELSE 0
                    END) AS successful_transactions,
                
                    SUM(CASE
                        WHEN t.status = 'FAILED' THEN 1
                        ELSE 0
                    END) AS failed_transactions
                
                FROM transactions t
                JOIN accounts a
                    ON t.sender_account_id = a.id
                
                WHERE a.account_number = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) ->
                        new TransactionSummaryResponse(
                                rs.getLong("total_transactions"),
                                rs.getBigDecimal("total_amount"),
                                rs.getLong("successful_transactions"),
                                rs.getLong("failed_transactions")
                        ),
                accountNumber
        );
    }

    public TransactionSummaryResponse getTransactionSummaryByDateRange(
            String accountNumber,
            LocalDateTime from,
            LocalDateTime to
    ) {

        String sql = """
                SELECT
                    COUNT(*) AS total_transactions,
                
                    COALESCE(
                        SUM(
                            CASE
                                WHEN t.status = 'SUCCESS'
                                THEN t.amount
                                ELSE 0
                            END
                        ),
                        0
                    ) AS total_amount,
                
                    SUM(CASE
                        WHEN t.status = 'SUCCESS' THEN 1
                        ELSE 0
                    END) AS successful_transactions,
                
                    SUM(CASE
                        WHEN t.status = 'FAILED' THEN 1
                        ELSE 0
                    END) AS failed_transactions
                
                FROM transactions t
                JOIN accounts a
                    ON t.sender_account_id = a.id
                
                WHERE a.account_number = ?
                  AND t.created_at BETWEEN ? AND ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) ->
                        new TransactionSummaryResponse(
                                rs.getLong("total_transactions"),
                                rs.getBigDecimal("total_amount"),
                                rs.getLong("successful_transactions"),
                                rs.getLong("failed_transactions")
                        ),
                accountNumber,
                from,
                to
        );
    }
}