package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.AccountDTOS;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@AllArgsConstructor
@RequestMapping(path = "/accounts" ,version = "1")
public class AccountController {
    private final AccountService accountService;

    @Operation(
            summary = "Get all accounts",
            description = "Retrieves a list of all accounts for the currently authenticated user.",
            operationId = "getAccountsForCurrentUser"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed")
            }
    )
    @GetMapping
    public ResponseEntity<List<AccountDTOS.Response>> getAccountsForCurrentUser(
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.getAccountsForCurrentUser(securityUser.getId()),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Get account by ID",
            description = "Retrieves a specific account by its ID for the currently authenticated user.",
            operationId = "getAccountById"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Account retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Account not found or does not belong to user")
            }
    )
    @GetMapping(path = "/{accountId}")
    public ResponseEntity<AccountDTOS.Response> getAccountById(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.getAccountById(accountId, securityUser.getId()),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Create an account",
            description = "Creates a new account for the currently authenticated user.",
            operationId = "createAccount"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Account created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "409", description = "Account name already exists for this user")
            }
    )
    @PostMapping
    public ResponseEntity<AccountDTOS.Response> createAccount(
            @RequestBody AccountDTOS.CreateRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.createAccount(request, securityUser.getId()),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Update an account",
            description = "Updates an existing account for the currently authenticated user.",
            operationId = "updateAccount"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Account updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Account not found or does not belong to user"),
                    @ApiResponse(responseCode = "409", description = "This user has an account with the same name")
            }
    )
    @PatchMapping(path = "/{accountId}")
    public ResponseEntity<AccountDTOS.Response> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountDTOS.UpdateRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.updateAccount(accountId, request, securityUser.getId()),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Delete an account",
            description = "Deletes a specific account by its ID for the currently authenticated user.",
            operationId = "deleteAccount"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Account not found or does not belong to user")
            }
    )
    @DeleteMapping(path = "/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        accountService.deleteAccount(accountId, securityUser.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
