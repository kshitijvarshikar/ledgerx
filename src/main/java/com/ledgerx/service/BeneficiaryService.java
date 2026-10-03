package com.ledgerx.service;

import com.ledgerx.entity.Account;
import com.ledgerx.entity.Beneficiary;
import com.ledgerx.entity.User;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.BeneficiaryRepository;
import com.ledgerx.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            UserRepository userRepository,
            AccountRepository accountRepository
    ) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Beneficiary addBeneficiary(
            String userEmail,
            String accountNumber,
            String nickname
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException("Beneficiary account not found"));

        if (account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You cannot add your own account as a beneficiary"
            );
        }

        if (beneficiaryRepository.existsByUserIdAndAccountNumber(
                user.getId(),
                accountNumber
        )) {
            throw new IllegalArgumentException(
                    "Beneficiary already exists"
            );
        }

        Beneficiary beneficiary = new Beneficiary();

        beneficiary.setUser(user);
        beneficiary.setAccountNumber(accountNumber);
        beneficiary.setNickname(nickname);
        beneficiary.setCreatedAt(LocalDateTime.now());

        return beneficiaryRepository.save(beneficiary);
    }

    public List<Beneficiary> getBeneficiaries(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return beneficiaryRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void deleteBeneficiary(
            String userEmail,
            Long beneficiaryId
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Beneficiary beneficiary =
                beneficiaryRepository
                        .findByIdAndUserId(
                                beneficiaryId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Beneficiary not found"
                                ));

        beneficiaryRepository.delete(beneficiary);
    }
}