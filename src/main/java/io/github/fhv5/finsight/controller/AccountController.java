package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.AccountDTOS;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Account", description = "Endpoints for managing user accounts")
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Retrieves a list of all accounts for the currently authenticated user")
    public ResponseEntity<List<AccountDTOS.Response>> getAccountsForCurrentUser(
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.getAccountsForCurrentUser(securityUser.getId()),
                HttpStatus.OK
        );
    }

    @GetMapping(path = "/{accountId}")
    @Operation(summary = "Get account by ID", description = "Retrieves a specific account by its ID for the currently authenticated user")
    public ResponseEntity<AccountDTOS.Response> getAccountById(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.getAccountById(accountId, securityUser.getId()),
                HttpStatus.OK
        );
    }

    @PostMapping
    @Operation(summary = "Create an account", description = "Creates a new account for the currently authenticated user")
    public ResponseEntity<AccountDTOS.Response> createAccount(
            @Valid @RequestBody AccountDTOS.CreateRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.createAccount(request, securityUser.getId()),
                HttpStatus.CREATED
        );
    }

    @PatchMapping(path = "/{accountId}")
    @Operation(summary = "Update an account", description = "Updates an existing account for the currently authenticated user")
    public ResponseEntity<AccountDTOS.Response> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountDTOS.UpdateRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.updateAccount(accountId, request, securityUser.getId()),
                HttpStatus.OK
        );
    }

    @DeleteMapping(path = "/{accountId}")
    @Operation(summary = "Delete an account", description = "Deletes a specific account by its ID for the currently authenticated user")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        accountService.deleteAccount(accountId, securityUser.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
