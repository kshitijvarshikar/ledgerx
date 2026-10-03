package com.ledgerx.service;

import com.ledgerx.entity.Account;
import com.ledgerx.entity.Beneficiary;
import com.ledgerx.entity.User;
import com.ledgerx.repository.AccountRepository;
import com.ledgerx.repository.BeneficiaryRepository;
import com.ledgerx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private BeneficiaryService beneficiaryService;

    private User user;
    private User otherUser;
    private Account beneficiaryAccount;

    @BeforeEach
    void setUp() {
        user = createUser(1L);
        otherUser = createUser(2L);

        beneficiaryAccount =
                createAccount("9876543210", otherUser);
    }

    @Test
    void shouldAddBeneficiarySuccessfully() {

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("9876543210"))
                .thenReturn(Optional.of(beneficiaryAccount));

        when(beneficiaryRepository
                .existsByUserIdAndAccountNumber(
                        1L,
                        "9876543210"
                ))
                .thenReturn(false);

        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Beneficiary result =
                beneficiaryService.addBeneficiary(
                        "user@example.com",
                        "9876543210",
                        "Friend"
                );

        assertNotNull(result);
        assertEquals("9876543210", result.getAccountNumber());
        assertEquals("Friend", result.getNickname());
        assertEquals(user, result.getUser());
        assertNotNull(result.getCreatedAt());

        verify(beneficiaryRepository)
                .save(any(Beneficiary.class));
    }

    @Test
    void shouldRejectWhenBeneficiaryAccountDoesNotExist() {

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("9876543210"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> beneficiaryService.addBeneficiary(
                                "user@example.com",
                                "9876543210",
                                "Friend"
                        )
                );

        assertEquals(
                "Beneficiary account not found",
                exception.getMessage()
        );

        verify(beneficiaryRepository, never())
                .save(any(Beneficiary.class));
    }

    @Test
    void shouldRejectAddingOwnAccount() {

        Account ownAccount =
                createAccount("1234567890", user);

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(ownAccount));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> beneficiaryService.addBeneficiary(
                                "user@example.com",
                                "1234567890",
                                "My Account"
                        )
                );

        assertEquals(
                "You cannot add your own account as a beneficiary",
                exception.getMessage()
        );

        verify(beneficiaryRepository, never())
                .save(any(Beneficiary.class));
    }

    @Test
    void shouldRejectDuplicateBeneficiary() {

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("9876543210"))
                .thenReturn(Optional.of(beneficiaryAccount));

        when(beneficiaryRepository
                .existsByUserIdAndAccountNumber(
                        1L,
                        "9876543210"
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> beneficiaryService.addBeneficiary(
                                "user@example.com",
                                "9876543210",
                                "Friend"
                        )
                );

        assertEquals(
                "Beneficiary already exists",
                exception.getMessage()
        );

        verify(beneficiaryRepository, never())
                .save(any(Beneficiary.class));
    }

    @Test
    void shouldReturnUserBeneficiaries() {

        Beneficiary beneficiary1 =
                createBeneficiary(
                        user,
                        "9876543210",
                        "Friend"
                );

        Beneficiary beneficiary2 =
                createBeneficiary(
                        user,
                        "5555555555",
                        "Office"
                );

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(beneficiaryRepository
                .findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        beneficiary1,
                        beneficiary2
                ));

        List<Beneficiary> result =
                beneficiaryService.getBeneficiaries(
                        "user@example.com"
                );

        assertEquals(2, result.size());
        assertEquals(
                "9876543210",
                result.get(0).getAccountNumber()
        );
        assertEquals(
                "5555555555",
                result.get(1).getAccountNumber()
        );

        verify(beneficiaryRepository)
                .findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void shouldDeleteBeneficiarySuccessfully() {

        Beneficiary beneficiary =
                createBeneficiary(
                        user,
                        "9876543210",
                        "Friend"
                );

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(beneficiaryRepository
                .findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(beneficiary));

        beneficiaryService.deleteBeneficiary(
                "user@example.com",
                1L
        );

        verify(beneficiaryRepository)
                .delete(beneficiary);
    }

    @Test
    void shouldRejectDeletingAnotherUsersBeneficiary() {

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(beneficiaryRepository
                .findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> beneficiaryService.deleteBeneficiary(
                                "user@example.com",
                                1L
                        )
                );

        assertEquals(
                "Beneficiary not found",
                exception.getMessage()
        );

        verify(beneficiaryRepository, never())
                .delete(any(Beneficiary.class));
    }

    private User createUser(Long id) {

        User user = new User(
                "Test User",
                id == 1L
                        ? "user@example.com"
                        : "other@example.com",
                "password"
        );

        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return user;
    }

    private Account createAccount(
            String accountNumber,
            User owner
    ) {

        Account account = new Account();

        account.setAccountNumber(accountNumber);
        account.setUser(owner);

        return account;
    }

    private Beneficiary createBeneficiary(
            User owner,
            String accountNumber,
            String nickname
    ) {

        Beneficiary beneficiary =
                new Beneficiary();

        beneficiary.setUser(owner);
        beneficiary.setAccountNumber(accountNumber);
        beneficiary.setNickname(nickname);

        return beneficiary;
    }
}