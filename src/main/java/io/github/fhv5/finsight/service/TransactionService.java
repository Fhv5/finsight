package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.TransactionDTOS;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.*;
import io.github.fhv5.finsight.projection.TransactionView;
import io.github.fhv5.finsight.repository.AccountRepository;
import io.github.fhv5.finsight.repository.CategoryRepository;
import io.github.fhv5.finsight.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public List<TransactionDTOS.Response> getTransactionsForCurrentUser(UUID userId) {
        List<TransactionView> transactions = transactionRepository.findAllByUserId(userId);

        return transactions.stream().map(transaction -> TransactionDTOS.Response.builder()
                .id(transaction.getId())
                .dateIssued(transaction.getDateIssued())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .originAccountId(transaction.getOriginAccountId())
                .originAccountName(transaction.getOriginAccountName())
                .destinationAccountId(transaction.getDestinationAccountId())
                .destinationAccountName(transaction.getDestinationAccountName())
                .categoryId(transaction.getCategoryId())
                .categoryName(transaction.getCategoryName())
                .build()).toList();
    }

    public TransactionDTOS.Response getTransactionById(
            UUID transactionId,
            UUID userId) {
        TransactionView transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or does not belong to user"));
        return TransactionDTOS.Response.builder()
                .id(transaction.getId())
                .dateIssued(transaction.getDateIssued())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .originAccountId(transaction.getOriginAccountId())
                .originAccountName(transaction.getOriginAccountName())
                .destinationAccountId(transaction.getDestinationAccountId())
                .destinationAccountName(transaction.getDestinationAccountName())
                .categoryId(transaction.getCategoryId())
                .categoryName(transaction.getCategoryName())
                .build();
    }

    @Transactional
    public TransactionDTOS.Response createIngreso(
            UUID userId,
            TransactionDTOS.CreateIngresoRequest request) {

        Category category = categoryRepository.findByIdAndUserIdAndType(
                        request.categoryId(), userId, CategoryType.INGRESO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category doesn't exist, does not belong to user, or is not of type INGRESO"));

        Account destinationAccount = accountRepository.findByIdAndUserId(request.destinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        Transaction newTransaction = Transaction.builder()
                .dateIssued(request.dateIssued())
                .type(TransactionType.INGRESO)
                .amount(request.amount())
                .description(request.description())
                .userId(userId)
                .destinationAccountId(request.destinationAccountId())
                .categoryId(request.categoryId())
                .build();

        destinationAccount.setBalance(destinationAccount.getBalance() + request.amount());

        accountRepository.save(destinationAccount);
        Transaction savedTransaction = transactionRepository.save(newTransaction);

        return TransactionDTOS.Response.builder()
                .id(savedTransaction.getId())
                .dateIssued(savedTransaction.getDateIssued())
                .type(savedTransaction.getType())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .destinationAccountId(savedTransaction.getDestinationAccountId())
                .destinationAccountName(destinationAccount.getName())
                .categoryId(savedTransaction.getCategoryId())
                .categoryName(category.getName())
                .build();
    }

    @Transactional
    public TransactionDTOS.Response createGasto(
            UUID userId, TransactionDTOS.CreateGastoRequest request) {

        Category category = categoryRepository.findByIdAndUserIdAndType(
                        request.categoryId(), userId, CategoryType.GASTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category doesn't exist, does not belong to user, or is not of type GASTO"));

        Account originAccount = accountRepository.findByIdAndUserId(request.originAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        Transaction newTransaction = Transaction.builder()
                .dateIssued(request.dateIssued())
                .type(TransactionType.GASTO)
                .amount(request.amount())
                .description(request.description())
                .userId(userId)
                .originAccountId(request.originAccountId())
                .categoryId(request.categoryId())
                .build();

        originAccount.setBalance(originAccount.getBalance() - request.amount());

        accountRepository.save(originAccount);
        Transaction savedTransaction = transactionRepository.save(newTransaction);

        return TransactionDTOS.Response.builder()
                .id(savedTransaction.getId())
                .dateIssued(savedTransaction.getDateIssued())
                .type(savedTransaction.getType())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .originAccountId(savedTransaction.getOriginAccountId())
                .originAccountName(originAccount.getName())
                .categoryId(savedTransaction.getCategoryId())
                .categoryName(category.getName())
                .resultedInNegativeBalance(originAccount.getBalance() < 0)
                .build();
    }

    @Transactional
    public TransactionDTOS.Response createTransferencia(
            UUID userId, TransactionDTOS.CreateTransferenciaRequest request) {

        Account originAccount = accountRepository.findByIdAndUserId(request.originAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        Account destinationAccount = accountRepository.findByIdAndUserId(request.destinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        Transaction newTransaction = Transaction.builder()
                .dateIssued(request.dateIssued())
                .type(TransactionType.TRANSFERENCIA)
                .amount(request.amount())
                .description(request.description())
                .userId(userId)
                .originAccountId(request.originAccountId())
                .destinationAccountId(request.destinationAccountId())
                .build();

        originAccount.setBalance(originAccount.getBalance() - request.amount());
        destinationAccount.setBalance(destinationAccount.getBalance() + request.amount());

        accountRepository.save(originAccount);
        accountRepository.save(destinationAccount);

        Transaction savedTransaction = transactionRepository.save(newTransaction);

        return TransactionDTOS.Response.builder()
                .id(savedTransaction.getId())
                .dateIssued(savedTransaction.getDateIssued())
                .type(savedTransaction.getType())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .originAccountId(savedTransaction.getOriginAccountId())
                .originAccountName(originAccount.getName())
                .destinationAccountId(savedTransaction.getDestinationAccountId())
                .destinationAccountName(destinationAccount.getName())
                .resultedInNegativeBalance(originAccount.getBalance() < 0)
                .build();
    }
}
