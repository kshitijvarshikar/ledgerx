package com.ledgerx.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET =
            "this-is-a-test-secret-key-for-jwt-service-123456789";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
    }

    @Test
    void shouldGenerateTokenSuccessfully() {

        String token = jwtService.generateToken(
                "john@example.com"
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractEmailFromToken() {

        String token = jwtService.generateToken(
                "john@example.com"
        );

        String email =
                jwtService.extractEmail(token);

        assertEquals(
                "john@example.com",
                email
        );
    }

    @Test
    void shouldValidateCorrectToken() {

        String token = jwtService.generateToken(
                "john@example.com"
        );

        assertTrue(
                jwtService.isTokenValid(
                        token,
                        "john@example.com"
                )
        );
    }

    @Test
    void shouldRejectTokenForDifferentEmail() {

        String token = jwtService.generateToken(
                "john@example.com"
        );

        assertFalse(
                jwtService.isTokenValid(
                        token,
                        "other@example.com"
                )
        );
    }

    @Test
    void shouldRejectInvalidToken() {

        assertFalse(
                jwtService.isTokenValid(
                        "invalid-token",
                        "john@example.com"
                )
        );
    }
}