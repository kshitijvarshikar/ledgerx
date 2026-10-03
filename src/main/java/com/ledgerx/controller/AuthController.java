package com.ledgerx.controller;

import com.ledgerx.dto.LoginRequest;
import com.ledgerx.dto.LoginResponse;
import com.ledgerx.dto.RegisterRequest;
import com.ledgerx.dto.UserResponse;
import com.ledgerx.service.AuthService;
import com.ledgerx.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "User registration and authentication APIs"
)
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(
            UserService userService,
            AuthService authService
    ) {
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new LedgerX user account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or email already registered"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Login",
            description = "Authenticates a user and returns a JWT access token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}