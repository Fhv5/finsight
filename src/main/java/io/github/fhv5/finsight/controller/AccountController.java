package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.AccountDTOS;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.AccountService;
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

    @GetMapping
    public ResponseEntity<List<AccountDTOS.Response>> getAccountsForCurrentUser(
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.getAccountsForCurrentUser(securityUser.getId()),
                HttpStatus.OK
        );
    }

    @GetMapping(path = "/{accountId}")
    public ResponseEntity<AccountDTOS.Response> getAccountById(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.getAccountById(accountId, securityUser.getId()),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<AccountDTOS.Response> createAccount(
            @RequestBody AccountDTOS.CreateRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                accountService.createAccount(request, securityUser.getId()),
                HttpStatus.CREATED
        );
    }

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

    @DeleteMapping(path = "/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        accountService.deleteAccount(accountId, securityUser.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
