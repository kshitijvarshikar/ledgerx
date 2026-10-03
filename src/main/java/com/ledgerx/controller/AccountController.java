package com.ledgerx.controller;

import com.ledgerx.dto.AccountResponse;
import com.ledgerx.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(
        name = "Accounts",
        description = "Account creation and account management APIs"
)
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(
            summary = "Create a new account",
            description = "Creates a new INR account for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            Authentication authentication
    ) {

        String email = authentication.getName();

        AccountResponse response =
                accountService.createAccount(email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get user accounts",
            description = "Returns all accounts belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts(
            Authentication authentication
    ) {

        String email = authentication.getName();

        List<AccountResponse> response =
                accountService.getUserAccounts(email);

        return ResponseEntity.ok(response);
    }
}