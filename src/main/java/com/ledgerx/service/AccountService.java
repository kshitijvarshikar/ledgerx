package com.ledgerx.service;

import com.ledgerx.dto.AccountResponse;
import com.ledgerx.entity.Account;
import com.ledgerx.entity.User;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public AccountResponse createAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency("INR");
        account.setUser(user);

        Account savedAccount =
                accountRepository.save(account);

        return new AccountResponse(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getBalance(),
                savedAccount.getCurrency(),
                savedAccount.getStatus()
        );
    }

    public List<AccountResponse> getUserAccounts(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return accountRepository
                .findByUserId(user.getId())
                .stream()
                .map(account ->
                        new AccountResponse(
                                account.getId(),
                                account.getAccountNumber(),
                                account.getBalance(),
                                account.getCurrency(),
                                account.getStatus()
                        )
                )
                .toList();
    }

    private String generateAccountNumber() {

        long number =
                1000000000L +
                        (long) (Math.random() * 9000000000L);

        String accountNumber =
                String.valueOf(number);

        if (accountRepository
                .existsByAccountNumber(accountNumber)) {

            return generateAccountNumber();
        }

        return accountNumber;
    }
}