package com.ledgerx.integration;

import com.ledgerx.dto.AccountResponse;
import com.ledgerx.dto.BeneficiaryRequest;
import com.ledgerx.dto.LoginRequest;
import com.ledgerx.dto.LoginResponse;
import com.ledgerx.dto.RegisterRequest;
import com.ledgerx.dto.TransactionResponse;
import com.ledgerx.dto.TransactionSummaryResponse;
import com.ledgerx.dto.TransferRequest;
import com.ledgerx.dto.UserResponse;
import com.ledgerx.entity.Account;
import com.ledgerx.entity.TransactionStatus;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.AuditLogRepository;
import com.ledgerx.repository.LedgerEntryRepository;
import com.ledgerx.repository.TransactionRepository;
import com.ledgerx.service.AccountService;
import com.ledgerx.service.AuthService;
import com.ledgerx.service.BeneficiaryService;
import com.ledgerx.service.TransactionHistoryService;
import com.ledgerx.service.TransactionReportService;
import com.ledgerx.service.TransactionService;
import com.ledgerx.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LedgerXIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionHistoryService transactionHistoryService;

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private TransactionReportService transactionReportService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;


    @Test
    void shouldRegisterAndLoginUser() {

        String email =
                "login" + System.currentTimeMillis()
                        + "@example.com";

        RegisterRequest request =
                new RegisterRequest();

        request.setName("Integration User");
        request.setEmail(email);
        request.setPassword("password123");

        UserResponse user =
                userService.registerUser(request);

        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(
                "Integration User",
                user.getName()
        );
        assertEquals(
                email,
                user.getEmail()
        );

        LoginRequest loginRequest =
                new LoginRequest();

        loginRequest.setEmail(email);
        loginRequest.setPassword("password123");

        LoginResponse response =
                authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());

        assertEquals(
                "Bearer",
                response.getTokenType()
        );
    }


    @Test
    void shouldCreateAccountForRegisteredUser() {

        String email =
                "account" + System.currentTimeMillis()
                        + "@example.com";

        RegisterRequest request =
                new RegisterRequest();

        request.setName("Account User");
        request.setEmail(email);
        request.setPassword("password123");

        userService.registerUser(request);

        AccountResponse account =
                accountService.createAccount(email);

        assertNotNull(account);
        assertNotNull(account.getId());
        assertNotNull(account.getAccountNumber());

        assertEquals(
                "INR",
                account.getCurrency()
        );

        assertEquals(
                "ACTIVE",
                account.getStatus().name()
        );

        assertEquals(
                BigDecimal.ZERO,
                account.getBalance()
        );
    }


    @Test
    void shouldTransferMoneyAndUpdateBalances() {

        String senderEmail =
                "sender" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("Sender", senderEmail);
        registerUser("Receiver", receiverEmail);

        AccountResponse sender =
                accountService.createAccount(senderEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        addBalance(
                sender.getAccountNumber(),
                new BigDecimal("1000.00")
        );

        TransferRequest request =
                createTransferRequest(
                        receiver.getAccountNumber(),
                        "250.00",
                        uniqueKey()
                );

        TransactionResponse response =
                transactionService.transfer(
                        senderEmail,
                        sender.getAccountNumber(),
                        request
                );

        assertEquals(
                TransactionStatus.SUCCESS,
                response.getStatus()
        );

        assertEquals(
                new BigDecimal("250.00"),
                response.getAmount()
        );

        AccountResponse updatedSender =
                accountService
                        .getUserAccounts(senderEmail)
                        .get(0);

        AccountResponse updatedReceiver =
                accountService
                        .getUserAccounts(receiverEmail)
                        .get(0);

        assertEquals(
                new BigDecimal("750.00"),
                updatedSender.getBalance()
        );

        assertEquals(
                new BigDecimal("250.00"),
                updatedReceiver.getBalance()
        );
    }


    @Test
    void shouldPreventDuplicateTransferUsingIdempotencyKey() {

        String senderEmail =
                "idem-sender" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "idem-receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("Sender", senderEmail);
        registerUser("Receiver", receiverEmail);

        AccountResponse sender =
                accountService.createAccount(senderEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        addBalance(
                sender.getAccountNumber(),
                new BigDecimal("1000.00")
        );

        String key = uniqueKey();

        TransferRequest request =
                createTransferRequest(
                        receiver.getAccountNumber(),
                        "250.00",
                        key
                );

        TransactionResponse first =
                transactionService.transfer(
                        senderEmail,
                        sender.getAccountNumber(),
                        request
                );

        TransactionResponse second =
                transactionService.transfer(
                        senderEmail,
                        sender.getAccountNumber(),
                        request
                );

        assertEquals(
                first.getId(),
                second.getId()
        );

        assertEquals(
                first.getTransactionReference(),
                second.getTransactionReference()
        );

        AccountResponse updatedSender =
                accountService
                        .getUserAccounts(senderEmail)
                        .get(0);

        AccountResponse updatedReceiver =
                accountService
                        .getUserAccounts(receiverEmail)
                        .get(0);

        assertEquals(
                new BigDecimal("750.00"),
                updatedSender.getBalance()
        );

        assertEquals(
                new BigDecimal("250.00"),
                updatedReceiver.getBalance()
        );

        assertTrue(
                transactionRepository
                        .findByIdempotencyKey(key)
                        .isPresent()
        );
    }


    @Test
    void shouldRejectInsufficientBalance() {

        String senderEmail =
                "low-balance" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "low-receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("Sender", senderEmail);
        registerUser("Receiver", receiverEmail);

        AccountResponse sender =
                accountService.createAccount(senderEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        addBalance(
                sender.getAccountNumber(),
                new BigDecimal("100.00")
        );

        TransferRequest request =
                createTransferRequest(
                        receiver.getAccountNumber(),
                        "500.00",
                        uniqueKey()
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                transactionService.transfer(
                                        senderEmail,
                                        sender.getAccountNumber(),
                                        request
                                )
                );

        assertEquals(
                "Insufficient balance",
                exception.getMessage()
        );
    }


    @Test
    void shouldRejectTransferFromAccountNotOwnedByUser() {

        String ownerEmail =
                "owner" + System.currentTimeMillis()
                        + "@example.com";

        String otherEmail =
                "other" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("Owner", ownerEmail);
        registerUser("Other", otherEmail);
        registerUser("Receiver", receiverEmail);

        AccountResponse ownerAccount =
                accountService.createAccount(ownerEmail);

        accountService.createAccount(otherEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        addBalance(
                ownerAccount.getAccountNumber(),
                new BigDecimal("1000.00")
        );

        TransferRequest request =
                createTransferRequest(
                        receiver.getAccountNumber(),
                        "100.00",
                        uniqueKey()
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                transactionService.transfer(
                                        otherEmail,
                                        ownerAccount.getAccountNumber(),
                                        request
                                )
                );

        assertEquals(
                "You do not own this account",
                exception.getMessage()
        );
    }


    @Test
    void shouldStoreTransactionLedgerAndAuditRecords() {

        String senderEmail =
                "records-sender" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "records-receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("Sender", senderEmail);
        registerUser("Receiver", receiverEmail);

        AccountResponse sender =
                accountService.createAccount(senderEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        addBalance(
                sender.getAccountNumber(),
                new BigDecimal("500.00")
        );

        TransferRequest request =
                createTransferRequest(
                        receiver.getAccountNumber(),
                        "100.00",
                        uniqueKey()
                );

        TransactionResponse response =
                transactionService.transfer(
                        senderEmail,
                        sender.getAccountNumber(),
                        request
                );

        assertTrue(
                transactionRepository
                        .findById(response.getId())
                        .isPresent()
        );

        assertFalse(
                ledgerEntryRepository
                        .findByTransactionId(response.getId())
                        .isEmpty()
        );

        assertFalse(
                auditLogRepository
                        .findByUserEmailOrderByCreatedAtDesc(
                                senderEmail
                        )
                        .isEmpty()
        );
    }


    @Test
    void shouldReturnTransactionHistory() {

        String senderEmail =
                "history-sender" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "history-receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("Sender", senderEmail);
        registerUser("Receiver", receiverEmail);

        AccountResponse sender =
                accountService.createAccount(senderEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        addBalance(
                sender.getAccountNumber(),
                new BigDecimal("1000.00")
        );

        TransferRequest request =
                createTransferRequest(
                        receiver.getAccountNumber(),
                        "200.00",
                        uniqueKey()
                );

        transactionService.transfer(
                senderEmail,
                sender.getAccountNumber(),
                request
        );

        Page<TransactionResponse> history =
                transactionHistoryService.getTransactionHistory(
                        senderEmail,
                        0,
                        10,
                        null,
                        null,
                        null
                );

        assertFalse(history.isEmpty());

        assertTrue(
                history.getTotalElements() >= 1
        );
    }


    @Test
    void shouldAddAndDeleteBeneficiary() {

        String userEmail =
                "beneficiary-user" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "beneficiary-receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("User", userEmail);
        registerUser("Receiver", receiverEmail);

        accountService.createAccount(userEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        BeneficiaryRequest request =
                new BeneficiaryRequest();

        request.setAccountNumber(
                receiver.getAccountNumber()
        );

        request.setNickname("My Receiver");

        var beneficiary =
                beneficiaryService.addBeneficiary(
                        userEmail,
                        request.getAccountNumber(),
                        request.getNickname()
                );

        assertNotNull(beneficiary);
        assertNotNull(beneficiary.getId());

        List<?> beneficiaries =
                beneficiaryService.getBeneficiaries(
                        userEmail
                );

        assertEquals(
                1,
                beneficiaries.size()
        );

        beneficiaryService.deleteBeneficiary(
                userEmail,
                beneficiary.getId()
        );

        assertTrue(
                beneficiaryService
                        .getBeneficiaries(userEmail)
                        .isEmpty()
        );
    }


    @Test
    void shouldGenerateTransactionReport() {

        String senderEmail =
                "report-sender" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "report-receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("Sender", senderEmail);
        registerUser("Receiver", receiverEmail);

        AccountResponse sender =
                accountService.createAccount(senderEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        addBalance(
                sender.getAccountNumber(),
                new BigDecimal("1000.00")
        );

        transactionService.transfer(
                senderEmail,
                sender.getAccountNumber(),
                createTransferRequest(
                        receiver.getAccountNumber(),
                        "300.00",
                        uniqueKey()
                )
        );

        BigDecimal total =
                transactionReportService
                        .getTotalTransferredAmount(
                                senderEmail,
                                sender.getAccountNumber()
                        );

        assertEquals(
                new BigDecimal("300.00"),
                total
        );

        TransactionSummaryResponse summary =
                transactionReportService
                        .getTransactionSummary(
                                senderEmail,
                                sender.getAccountNumber()
                        );

        assertNotNull(summary);

        assertEquals(
                1,
                summary.getTotalTransactions()
        );
    }


    @Test
    void shouldGenerateDateRangeReport() {

        String senderEmail =
                "date-report-sender" + System.currentTimeMillis()
                        + "@example.com";

        String receiverEmail =
                "date-report-receiver" + System.currentTimeMillis()
                        + "@example.com";

        registerUser("Sender", senderEmail);
        registerUser("Receiver", receiverEmail);

        AccountResponse sender =
                accountService.createAccount(senderEmail);

        AccountResponse receiver =
                accountService.createAccount(receiverEmail);

        addBalance(
                sender.getAccountNumber(),
                new BigDecimal("1000.00")
        );

        transactionService.transfer(
                senderEmail,
                sender.getAccountNumber(),
                createTransferRequest(
                        receiver.getAccountNumber(),
                        "150.00",
                        uniqueKey()
                )
        );

        LocalDateTime from =
                LocalDateTime.now().minusMinutes(5);

        LocalDateTime to =
                LocalDateTime.now().plusMinutes(5);

        TransactionSummaryResponse summary =
                transactionReportService
                        .getTransactionSummaryByDateRange(
                                senderEmail,
                                sender.getAccountNumber(),
                                from,
                                to
                        );

        assertNotNull(summary);

        assertEquals(
                1,
                summary.getTotalTransactions()
        );
    }


    private void registerUser(
            String name,
            String email
    ) {

        RegisterRequest request =
                new RegisterRequest();

        request.setName(name);
        request.setEmail(email);
        request.setPassword("password123");

        userService.registerUser(request);
    }


    private TransferRequest createTransferRequest(
            String receiverAccountNumber,
            String amount,
            String idempotencyKey
    ) {

        TransferRequest request =
                new TransferRequest();

        request.setReceiverAccountNumber(
                receiverAccountNumber
        );

        request.setAmount(
                new BigDecimal(amount)
        );

        request.setIdempotencyKey(
                idempotencyKey
        );

        return request;
    }


    private String uniqueKey() {

        return "integration-"
                + System.currentTimeMillis()
                + "-"
                + System.nanoTime();
    }


    private void addBalance(
            String accountNumber,
            BigDecimal amount
    ) {

        Account account =
                accountRepository
                        .findByAccountNumber(accountNumber)
                        .orElseThrow();

        account.setBalance(
                account.getBalance().add(amount)
        );

        accountRepository.save(account);
    }
}