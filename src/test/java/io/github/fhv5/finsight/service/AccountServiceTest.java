package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.AccountDTOS;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.exception.ResourceAlreadyExistsException;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.Account;
import io.github.fhv5.finsight.model.AccountType;
import io.github.fhv5.finsight.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private UUID userId;
    private UUID accountId;
    private Account mockAccount;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        mockAccount = Account.builder()
                .id(accountId)
                .name("Main Account")
                .description("Primary checking account")
                .balance(1000L)
                .type(AccountType.REGULAR)
                .userId(userId)
                .build();
    }

    @Test
    void getAccountsForCurrentUser_ShouldReturnListOfAccounts() {
        when(accountRepository.findAllByUserId(userId)).thenReturn(List.of(mockAccount));

        List<AccountDTOS.Response> accounts = accountService.getAccountsForCurrentUser(userId);

        assertEquals(1, accounts.size());
        assertEquals(accountId, accounts.getFirst().id());
        assertEquals("Main Account", accounts.getFirst().name());
        verify(accountRepository).findAllByUserId(userId);
    }

    @Test
    void getAccountById_ShouldReturnAccount_WhenValidIdAndUserId() {
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(mockAccount));

        AccountDTOS.Response response = accountService.getAccountById(accountId, userId);

        assertNotNull(response);
        assertEquals(accountId, response.id());
        verify(accountRepository).findByIdAndUserId(accountId, userId);
    }

    @Test
    void getAccountById_ShouldThrowException_WhenNotFound() {
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(accountId, userId));
    }

    @Test
    void createAccount_ShouldSaveAndReturnAccount_WhenValid() {
        AccountDTOS.CreateRequest request = new AccountDTOS.CreateRequest("Savings", "Savings account", 500L);

        when(accountRepository.existsByUserIdAndName(userId, "Savings")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(UUID.randomUUID());
            return account;
        });

        AccountDTOS.Response response = accountService.createAccount(request, userId);

        assertNotNull(response);
        assertEquals("Savings", response.name());
        assertEquals("Savings account", response.description());
        assertEquals(500L, response.balance());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldThrowException_WhenNameAlreadyExists() {
        AccountDTOS.CreateRequest request = new AccountDTOS.CreateRequest("Main Account", "Test", 100L);

        when(accountRepository.existsByUserIdAndName(userId, "Main Account")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> accountService.createAccount(request, userId));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void updateAccount_ShouldUpdateAndReturnAccount_WhenValid() {
        AccountDTOS.UpdateRequest request = new AccountDTOS.UpdateRequest("Updated Account", "Updated description");

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.existsByUserIdAndName(userId, "Updated Account")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        AccountDTOS.Response response = accountService.updateAccount(accountId, request, userId);

        assertNotNull(response);
        assertEquals("Updated Account", mockAccount.getName()); // Since we modified mockAccount directly in service
        assertEquals("Updated description", mockAccount.getDescription());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_ShouldThrowException_WhenNameAlreadyExists() {
        AccountDTOS.UpdateRequest request = new AccountDTOS.UpdateRequest("Existing Account", null);

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.existsByUserIdAndName(userId, "Existing Account")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> accountService.updateAccount(accountId, request, userId));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deleteAccount_ShouldDelete_WhenValid() {
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(mockAccount));

        accountService.deleteAccount(accountId, userId);

        verify(accountRepository).delete(mockAccount);
    }

    // ─── createSavingsAccount ──────────────────────────────────────────────────

    @Test
    void createSavingsAccount_ShouldSaveAndReturnSavingsAccount_WhenValid() {
        AccountDTOS.CreateSavingsRequest request =
                new AccountDTOS.CreateSavingsRequest("Emergency Fund", "For emergencies", 10000L);

        when(accountRepository.existsByUserIdAndName(userId, "Emergency Fund")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(UUID.randomUUID());
            return account;
        });

        AccountDTOS.Response response = accountService.createSavingsAccount(request, userId);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.name());
        assertEquals(0L, response.balance());
        assertEquals(10000L, response.targetAmount());
        assertEquals(AccountType.AHORRO, response.type());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createSavingsAccount_ShouldThrowException_WhenNameAlreadyExists() {
        AccountDTOS.CreateSavingsRequest request =
                new AccountDTOS.CreateSavingsRequest("Main Account", "Desc", 5000L);

        when(accountRepository.existsByUserIdAndName(userId, "Main Account")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> accountService.createSavingsAccount(request, userId));
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ─── deleteAccount savings guard ───────────────────────────────────────────

    @Test
    void deleteAccount_ShouldThrowException_WhenSavingsAccountHasPositiveBalance() {
        Account savingsAccount = Account.builder()
                .id(accountId)
                .name("Emergency Fund")
                .description("For emergencies")
                .balance(500L)
                .type(AccountType.AHORRO)
                .userId(userId)
                .build();
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(savingsAccount));

        assertThrows(InvalidInputException.class,
                () -> accountService.deleteAccount(accountId, userId));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    void deleteAccount_ShouldDelete_WhenSavingsAccountBalanceIsZero() {
        Account savingsAccount = Account.builder()
                .id(accountId)
                .name("Empty Savings")
                .description("Empty")
                .balance(0L)
                .type(AccountType.AHORRO)
                .userId(userId)
                .build();
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(savingsAccount));

        accountService.deleteAccount(accountId, userId);

        verify(accountRepository).delete(savingsAccount);
    }
}
