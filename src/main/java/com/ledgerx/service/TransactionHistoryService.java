package com.ledgerx.service;

import com.ledgerx.dto.TransactionResponse;
import com.ledgerx.entity.Account;
import com.ledgerx.entity.Transaction;
import com.ledgerx.entity.TransactionStatus;
import com.ledgerx.entity.User;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.TransactionRepository;
import com.ledgerx.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionHistoryService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public TransactionHistoryService(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            AccountRepository accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public Page<TransactionResponse> getTransactionHistory(
            String userEmail,
            int page,
            int size,
            TransactionStatus status,
            LocalDateTime from,
            LocalDateTime to
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<Account> accounts =
                accountRepository.findByUserId(user.getId());

        if (accounts.isEmpty()) {
            return Page.empty();
        }

        List<Long> accountIds =
                accounts.stream()
                        .map(Account::getId)
                        .toList();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Transaction> transactions;

        if (from != null && to != null && status != null) {

            transactions =
                    transactionRepository
                            .findByAccountIdsAndStatusAndCreatedAtBetween(
                                    accountIds,
                                    status,
                                    from,
                                    to,
                                    pageable
                            );

        } else if (from != null && to != null) {

            transactions =
                    transactionRepository
                            .findByAccountIdsAndCreatedAtBetween(
                                    accountIds,
                                    from,
                                    to,
                                    pageable
                            );

        } else if (status != null) {

            transactions =
                    transactionRepository
                            .findByAccountIdsAndStatus(
                                    accountIds,
                                    status,
                                    pageable
                            );

        } else {

            transactions =
                    transactionRepository
                            .findByAccountIds(
                                    accountIds,
                                    pageable
                            );
        }

        return transactions.map(transaction ->
                new TransactionResponse(
                        transaction.getId(),
                        transaction.getTransactionReference(),
                        transaction.getSenderAccount().getAccountNumber(),
                        transaction.getReceiverAccount().getAccountNumber(),
                        transaction.getAmount(),
                        transaction.getStatus(),
                        transaction.getCreatedAt()
                )
        );
    }
}