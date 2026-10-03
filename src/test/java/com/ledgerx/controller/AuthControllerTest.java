package com.ledgerx.controller;

import com.ledgerx.dto.LoginRequest;
import com.ledgerx.dto.LoginResponse;
import com.ledgerx.dto.RegisterRequest;
import com.ledgerx.dto.UserResponse;
import com.ledgerx.service.AuthService;
import com.ledgerx.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request =
                new RegisterRequest();

        request.setName("Test User");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        UserResponse userResponse =
                new UserResponse(
                        1L,
                        "Test User",
                        "user@example.com"
                );

        when(userService.registerUser(request))
                .thenReturn(userResponse);

        var response =
                authController.register(request);

        assertEquals(
                201,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                1L,
                response.getBody().getId()
        );

        assertEquals(
                "Test User",
                response.getBody().getName()
        );

        assertEquals(
                "user@example.com",
                response.getBody().getEmail()
        );

        verify(userService)
                .registerUser(request);
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request =
                new LoginRequest();

        request.setEmail("user@example.com");
        request.setPassword("password123");

        LoginResponse loginResponse =
                new LoginResponse(
                        "test-jwt-token",
                        "Bearer"
                );

        when(authService.login(request))
                .thenReturn(loginResponse);

        var response =
                authController.login(request);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "test-jwt-token",
                response.getBody().getToken()
        );

        assertEquals(
                "Bearer",
                response.getBody().getTokenType()
        );

        verify(authService)
                .login(request);
    }

    @Test
    void shouldPropagateRegistrationError() {

        RegisterRequest request =
                new RegisterRequest();

        request.setName("Test User");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userService.registerUser(request))
                .thenThrow(
                        new IllegalArgumentException(
                                "Email already registered"
                        )
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authController.register(request)
                );

        assertEquals(
                "Email already registered",
                exception.getMessage()
        );

        verify(userService)
                .registerUser(request);
    }

    @Test
    void shouldPropagateLoginError() {

        LoginRequest request =
                new LoginRequest();

        request.setEmail("user@example.com");
        request.setPassword("wrongpassword");

        when(authService.login(request))
                .thenThrow(
                        new IllegalArgumentException(
                                "Invalid email or password"
                        )
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authController.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(authService)
                .login(request);
    }
}