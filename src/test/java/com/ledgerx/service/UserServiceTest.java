package com.ledgerx.service;

import com.ledgerx.dto.RegisterRequest;
import com.ledgerx.dto.UserResponse;
import com.ledgerx.entity.User;
import com.ledgerx.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        User savedUser = new User(
                "John Doe",
                "john@example.com",
                "encodedPassword"
        );

        UserResponse expectedResponse;

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    User saved = new User(
                            user.getName(),
                            user.getEmail(),
                            user.getPassword()
                    );

                    try {
                        var idField = User.class.getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(saved, 1L);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return saved;
                });

        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(request)
        );

        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldEncodePasswordBeforeSaving() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("plainPassword");

        when(userRepository.existsByEmail("jane@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.registerUser(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("Jane Doe", savedUser.getName());
        assertEquals("jane@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());

        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    void shouldReturnSavedUserDetails() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("alice@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        User savedUser = new User(
                "Alice",
                "alice@example.com",
                "encodedPassword"
        );

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("Alice", response.getName());
        assertEquals("alice@example.com", response.getEmail());
    }
}