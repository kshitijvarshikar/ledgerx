package com.ledgerx.repository;

import com.ledgerx.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountNumber = :accountNumber
            """)
    Optional<Account> findByAccountNumberForUpdate(
            @Param("accountNumber") String accountNumber
    );

    List<Account> findByUserId(Long userId);
}