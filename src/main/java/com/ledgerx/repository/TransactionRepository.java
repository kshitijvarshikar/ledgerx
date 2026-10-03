package com.ledgerx.repository;

import com.ledgerx.entity.Transaction;
import com.ledgerx.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.support.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(
            String transactionReference
    );

    Optional<Transaction> findByIdempotencyKey(
            String idempotencyKey
    );

    @EntityGraph(attributePaths = {"senderAccount", "receiverAccount"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.senderAccount.id IN :accountIds
               OR t.receiverAccount.id IN :accountIds
            """)
    Page<Transaction> findByAccountIds(
            @Param("accountIds") List<Long> accountIds,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"senderAccount", "receiverAccount"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE (t.senderAccount.id IN :accountIds
               OR t.receiverAccount.id IN :accountIds)
              AND t.status = :status
            """)
    Page<Transaction> findByAccountIdsAndStatus(
            @Param("accountIds") List<Long> accountIds,
            @Param("status") TransactionStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"senderAccount", "receiverAccount"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE (t.senderAccount.id IN :accountIds
               OR t.receiverAccount.id IN :accountIds)
              AND t.createdAt BETWEEN :from AND :to
            """)
    Page<Transaction> findByAccountIdsAndCreatedAtBetween(
            @Param("accountIds") List<Long> accountIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"senderAccount", "receiverAccount"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE (t.senderAccount.id IN :accountIds
               OR t.receiverAccount.id IN :accountIds)
              AND t.status = :status
              AND t.createdAt BETWEEN :from AND :to
            """)
    Page<Transaction> findByAccountIdsAndStatusAndCreatedAtBetween(
            @Param("accountIds") List<Long> accountIds,
            @Param("status") TransactionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}