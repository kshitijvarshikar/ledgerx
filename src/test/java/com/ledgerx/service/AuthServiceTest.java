package com.ledgerx.service;

import com.ledgerx.dto.LoginRequest;
import com.ledgerx.dto.LoginResponse;
import com.ledgerx.entity.User;
import com.ledgerx.repository.UserRepository;
import com.ledgerx.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        User user = new User(
                "John Doe",
                "john@example.com",
                "encodedPassword"
        );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken("john@example.com"))
                .thenReturn("test-jwt-token");

        LoginResponse response =
                authService.login(request);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());

        verify(userRepository)
                .findByEmail("john@example.com");

        verify(passwordEncoder)
                .matches(
                        "password123",
                        "encodedPassword"
                );

        verify(jwtService)
                .generateToken("john@example.com");
    }

    @Test
    void shouldRejectUnknownEmail() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("unknown@example.com");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(anyString());
    }

    @Test
    void shouldRejectIncorrectPassword() {

        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrongPassword");

        User user = new User(
                "John Doe",
                "john@example.com",
                "encodedPassword"
        );

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(passwordEncoder)
                .matches(
                        "wrongPassword",
                        "encodedPassword"
                );

        verify(jwtService, never())
                .generateToken(anyString());
    }

    @Test
    void shouldGenerateJwtTokenAfterSuccessfulPasswordCheck() {

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        User user = new User(
                "Alice",
                "alice@example.com",
                "encodedPassword"
        );

        when(userRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken("alice@example.com"))
                .thenReturn("alice-jwt-token");

        LoginResponse response =
                authService.login(request);

        assertEquals(
                "alice-jwt-token",
                response.getToken()
        );

        verify(jwtService)
                .generateToken("alice@example.com");
    }
}