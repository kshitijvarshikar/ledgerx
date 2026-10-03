package com.ledgerx.controller;

import com.ledgerx.dto.BeneficiaryRequest;
import com.ledgerx.dto.BeneficiaryResponse;
import com.ledgerx.entity.Beneficiary;
import com.ledgerx.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@Tag(
        name = "Beneficiaries",
        description = "Beneficiary management APIs"
)
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(
            BeneficiaryService beneficiaryService
    ) {
        this.beneficiaryService = beneficiaryService;
    }

    @Operation(
            summary = "Add a beneficiary",
            description = "Adds a beneficiary account for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Beneficiary added successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BeneficiaryResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, duplicate beneficiary, or own account"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @PostMapping
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(
            Authentication authentication,
            @Valid @RequestBody BeneficiaryRequest request
    ) {

        String email = authentication.getName();

        Beneficiary beneficiary =
                beneficiaryService.addBeneficiary(
                        email,
                        request.getAccountNumber(),
                        request.getNickname()
                );

        BeneficiaryResponse response =
                new BeneficiaryResponse(
                        beneficiary.getId(),
                        beneficiary.getAccountNumber(),
                        beneficiary.getNickname(),
                        beneficiary.getCreatedAt()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get beneficiaries",
            description = "Returns all beneficiaries belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Beneficiaries retrieved successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = BeneficiaryResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> getBeneficiaries(
            Authentication authentication
    ) {

        String email = authentication.getName();

        List<BeneficiaryResponse> response =
                beneficiaryService
                        .getBeneficiaries(email)
                        .stream()
                        .map(beneficiary ->
                                new BeneficiaryResponse(
                                        beneficiary.getId(),
                                        beneficiary.getAccountNumber(),
                                        beneficiary.getNickname(),
                                        beneficiary.getCreatedAt()
                                )
                        )
                        .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a beneficiary",
            description = "Deletes a beneficiary belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Beneficiary deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Beneficiary not found or invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(
            Authentication authentication,
            @PathVariable Long id
    ) {

        String email = authentication.getName();

        beneficiaryService.deleteBeneficiary(
                email,
                id
        );

        return ResponseEntity.noContent().build();
    }
}