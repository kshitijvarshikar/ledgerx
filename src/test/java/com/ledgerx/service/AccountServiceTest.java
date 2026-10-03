package com.ledgerx.service;

import com.ledgerx.dto.AccountResponse;
import com.ledgerx.entity.Account;
import com.ledgerx.entity.AccountStatus;
import com.ledgerx.entity.User;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() {

        User user = new User(
                "John Doe",
                "john@example.com",
                "encodedPassword"
        );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(false);

        Account savedAccount = new Account();
        savedAccount.setAccountNumber("1234567890");
        savedAccount.setBalance(BigDecimal.ZERO);
        savedAccount.setCurrency("INR");
        savedAccount.setUser(user);

        when(accountRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        AccountResponse response =
                accountService.createAccount("john@example.com");

        assertNotNull(response);
        assertEquals("1234567890", response.getAccountNumber());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        assertEquals("INR", response.getCurrency());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());

        verify(userRepository).findByEmail("john@example.com");
        verify(accountRepository).existsByAccountNumber(anyString());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldRejectAccountCreationWhenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount("unknown@example.com")
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("unknown@example.com");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldGenerateAnotherAccountNumberWhenNumberAlreadyExists() {

        User user = new User(
                "John Doe",
                "john@example.com",
                "encodedPassword"
        );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true, false);

        Account savedAccount = new Account();
        savedAccount.setAccountNumber("9876543210");
        savedAccount.setBalance(BigDecimal.ZERO);
        savedAccount.setCurrency("INR");
        savedAccount.setUser(user);

        when(accountRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        AccountResponse response =
                accountService.createAccount("john@example.com");

        assertNotNull(response);
        assertEquals("9876543210", response.getAccountNumber());

        verify(accountRepository, times(2))
                .existsByAccountNumber(anyString());

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldGetUserAccountsSuccessfully() {

        User user = new User(
                "John Doe",
                "john@example.com",
                "encodedPassword"
        );

        Account account1 = new Account();
        account1.setAccountNumber("1234567890");
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setCurrency("INR");
        account1.setUser(user);

        Account account2 = new Account();
        account2.setAccountNumber("9876543210");
        account2.setBalance(new BigDecimal("500.00"));
        account2.setCurrency("INR");
        account2.setUser(user);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(user.getId()))
                .thenReturn(List.of(account1, account2));

        List<AccountResponse> response =
                accountService.getUserAccounts("john@example.com");

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(
                "1234567890",
                response.get(0).getAccountNumber()
        );

        assertEquals(
                "9876543210",
                response.get(1).getAccountNumber()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                response.get(0).getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                response.get(1).getBalance()
        );

        verify(userRepository).findByEmail("john@example.com");
        verify(accountRepository).findByUserId(user.getId());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoAccounts() {

        User user = new User(
                "John Doe",
                "john@example.com",
                "encodedPassword"
        );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserId(user.getId()))
                .thenReturn(List.of());

        List<AccountResponse> response =
                accountService.getUserAccounts("john@example.com");

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(userRepository).findByEmail("john@example.com");
        verify(accountRepository).findByUserId(user.getId());
    }

    @Test
    void shouldRejectGettingAccountsWhenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getUserAccounts("unknown@example.com")
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("unknown@example.com");
        verify(accountRepository, never()).findByUserId(anyLong());
    }
}