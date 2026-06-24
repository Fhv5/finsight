package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.AccountDTOS;
import io.github.fhv5.finsight.model.AccountType;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private SecurityUser securityUser;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        securityUser = new SecurityUser(user);
        accountId = UUID.randomUUID();
    }

    @Test
    void getAccountsForCurrentUser_ShouldReturnOkStatusAndList() {
        AccountDTOS.Response responseItem = AccountDTOS.Response.builder()
                .id(accountId)
                .name("Test Account")
                .balance(100L)
                .build();
        List<AccountDTOS.Response> expectedResponse = List.of(responseItem);

        when(accountService.getAccountsForCurrentUser(userId)).thenReturn(expectedResponse);

        ResponseEntity<List<AccountDTOS.Response>> result = accountController.getAccountsForCurrentUser(securityUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void getAccountById_ShouldReturnOkStatusAndAccount() {
        AccountDTOS.Response expectedResponse = AccountDTOS.Response.builder()
                .id(accountId)
                .name("Test Account")
                .balance(100L)
                .build();

        when(accountService.getAccountById(accountId, userId)).thenReturn(expectedResponse);

        ResponseEntity<AccountDTOS.Response> result = accountController.getAccountById(accountId, securityUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void createAccount_ShouldReturnCreatedStatusAndAccount() {
        AccountDTOS.CreateRequest request = new AccountDTOS.CreateRequest("Test Account", "Desc", 100L);
        AccountDTOS.Response expectedResponse = AccountDTOS.Response.builder()
                .id(accountId)
                .name("Test Account")
                .description("Desc")
                .balance(100L)
                .type(AccountType.REGULAR)
                .build();

        when(accountService.createAccount(any(AccountDTOS.CreateRequest.class), eq(userId))).thenReturn(expectedResponse);

        ResponseEntity<AccountDTOS.Response> result = accountController.createAccount(request, securityUser);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void createSavingsAccount_ShouldReturnCreatedStatusAndSavingsAccount() {
        AccountDTOS.CreateSavingsRequest request =
                new AccountDTOS.CreateSavingsRequest("Emergency Fund", "For emergencies", 10000L);
        AccountDTOS.Response expectedResponse = AccountDTOS.Response.builder()
                .id(accountId)
                .name("Emergency Fund")
                .description("For emergencies")
                .balance(0L)
                .targetAmount(10000L)
                .type(AccountType.AHORRO)
                .build();

        when(accountService.createSavingsAccount(any(AccountDTOS.CreateSavingsRequest.class), eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<AccountDTOS.Response> result = accountController.createSavingsAccount(request, securityUser);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(accountService).createSavingsAccount(any(AccountDTOS.CreateSavingsRequest.class), eq(userId));
    }

    @Test
    void updateAccount_ShouldReturnOkStatusAndAccount() {
        AccountDTOS.UpdateRequest request = new AccountDTOS.UpdateRequest("Updated Account", "Updated Desc");
        AccountDTOS.Response expectedResponse = AccountDTOS.Response.builder()
                .id(accountId)
                .name("Updated Account")
                .description("Updated Desc")
                .balance(100L)
                .build();

        when(accountService.updateAccount(eq(accountId), any(AccountDTOS.UpdateRequest.class), eq(userId))).thenReturn(expectedResponse);

        ResponseEntity<AccountDTOS.Response> result = accountController.updateAccount(accountId, request, securityUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void deleteAccount_ShouldReturnNoContentStatus() {
        ResponseEntity<Void> result = accountController.deleteAccount(accountId, securityUser);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(accountService).deleteAccount(accountId, userId);
    }
}
