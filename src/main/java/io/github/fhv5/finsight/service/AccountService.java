package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.AccountDTOS;
import io.github.fhv5.finsight.exception.ResourceAlreadyExistsException;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.Account;
import io.github.fhv5.finsight.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<AccountDTOS.Response> getAccountsForCurrentUser(
            UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        return accounts.stream()
                .map(account -> AccountDTOS.Response.builder()
                        .id(account.getId())
                        .name(account.getName())
                        .description(account.getDescription())
                        .balance(account.getBalance())
                        .build())
                .toList();
    }

    public AccountDTOS.Response getAccountById(
            UUID accountId,
            UUID userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
               .orElseThrow(() -> new ResourceNotFoundException("Account not found or does not belong to user"));

        return AccountDTOS.Response.builder()
                .id(account.getId())
                .name(account.getName())
                .description(account.getDescription())
                .balance(account.getBalance())
                .build();
    }

    public AccountDTOS.Response createAccount(
            AccountDTOS.CreateRequest request,
            UUID userId) {

        if (accountRepository.existsByUserIdAndName(userId, request.name())) {
            throw new ResourceAlreadyExistsException("Account name already exists for this user");
        }

        Account newAccount = Account.builder()
                .name(request.name())
                .description(request.description())
                .balance(request.balance())
                .userId(userId)
                .build();

        Account savedAccount = accountRepository.save(newAccount);

        return AccountDTOS.Response.builder()
                .id(savedAccount.getId())
                .name(savedAccount.getName())
                .description(savedAccount.getDescription())
                .balance(savedAccount.getBalance())
                .build();

    }


    public AccountDTOS.Response updateAccount(
            UUID accountId,
            AccountDTOS.UpdateRequest request,
            UUID userId) {
        Account existingAccount = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or does not belong to user"));

        if (request.name() != null && !request.name().equals(existingAccount.getName())) {
            if (accountRepository.existsByUserIdAndName(userId, request.name())) {
                throw new ResourceAlreadyExistsException("This user has an account with the same name");
            }
            existingAccount.setName(request.name());
        }

        if (request.description() != null) {
            existingAccount.setDescription(request.description());
        }

        Account updatedAccount = accountRepository.save(existingAccount);

        return AccountDTOS.Response.builder()
                .id(updatedAccount.getId())
                .name(updatedAccount.getName())
                .description(updatedAccount.getDescription())
                .balance(updatedAccount.getBalance())
                .build();
    }

    public void deleteAccount(
            UUID accountId,
            UUID userId) {
        Account existingAccount = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or does not belong to user"));

        accountRepository.delete(existingAccount);
    }
}
