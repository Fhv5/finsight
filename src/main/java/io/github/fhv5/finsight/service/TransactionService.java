package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.TransactionDTOS;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.*;
import io.github.fhv5.finsight.projection.TransactionView;
import io.github.fhv5.finsight.repository.AccountRepository;
import io.github.fhv5.finsight.repository.CategoryRepository;
import io.github.fhv5.finsight.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
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

        Transaction newTransaction = Transaction.builder()
                .dateIssued(request.dateIssued())
                .type(TransactionType.INGRESO)
                .amount(request.amount())
                .description(request.description())
                .userId(userId)
                .destinationAccountId(request.destinationAccountId())
                .categoryId(request.categoryId())
                .build();

        TransactionDTOS.TransactionContext context = applyIngreso(newTransaction, userId);

        Transaction savedTransaction = transactionRepository.save(newTransaction);

        return TransactionDTOS.Response.builder()
                .id(savedTransaction.getId())
                .dateIssued(savedTransaction.getDateIssued())
                .type(savedTransaction.getType())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .destinationAccountId(savedTransaction.getDestinationAccountId())
                .destinationAccountName(context.destination().getName())
                .categoryId(savedTransaction.getCategoryId())
                .categoryName(context.category().getName())
                .build();
    }

    @Transactional
    public TransactionDTOS.Response createGasto(
            UUID userId, TransactionDTOS.CreateGastoRequest request) {

        Transaction newTransaction = Transaction.builder()
                .dateIssued(request.dateIssued())
                .type(TransactionType.GASTO)
                .amount(request.amount())
                .description(request.description())
                .userId(userId)
                .originAccountId(request.originAccountId())
                .categoryId(request.categoryId())
                .build();

        TransactionDTOS.TransactionContext context = applyGasto(newTransaction, userId);
        Transaction savedTransaction = transactionRepository.save(newTransaction);

        return TransactionDTOS.Response.builder()
                .id(savedTransaction.getId())
                .dateIssued(savedTransaction.getDateIssued())
                .type(savedTransaction.getType())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .originAccountId(savedTransaction.getOriginAccountId())
                .originAccountName(context.origin().getName())
                .categoryId(savedTransaction.getCategoryId())
                .categoryName(context.category().getName())
                .resultedInNegativeBalance(context.origin().getBalance() < 0)
                .build();
    }

    @Transactional
    public TransactionDTOS.Response createTransferencia(
            UUID userId, TransactionDTOS.CreateTransferenciaRequest request) {
        Transaction newTransaction = Transaction.builder()
                .dateIssued(request.dateIssued())
                .type(TransactionType.TRANSFERENCIA)
                .amount(request.amount())
                .description(request.description())
                .userId(userId)
                .originAccountId(request.originAccountId())
                .destinationAccountId(request.destinationAccountId())
                .build();

        TransactionDTOS.TransactionContext context = applyTransferencia(newTransaction, userId);
        Transaction savedTransaction = transactionRepository.save(newTransaction);

        return TransactionDTOS.Response.builder()
                .id(savedTransaction.getId())
                .dateIssued(savedTransaction.getDateIssued())
                .type(savedTransaction.getType())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .originAccountId(savedTransaction.getOriginAccountId())
                .originAccountName(context.origin().getName())
                .destinationAccountId(savedTransaction.getDestinationAccountId())
                .destinationAccountName(context.destination().getName())
                .resultedInNegativeBalance(context.origin().getBalance() < 0)
                .build();
    }

    @Transactional
    public TransactionDTOS.Response updateTransaction(
            UUID userId,
            UUID transactionId,
            TransactionDTOS.UpdateRequest request) {

        Transaction transaction = transactionRepository.findEntityByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or does not belong to user"));

        revertTransaction(transaction, userId);

        if (request.dateIssued() != null) transaction.setDateIssued(request.dateIssued());
        if (request.description() != null) transaction.setDescription(request.description());
        if (request.type() != null) transaction.setType(request.type());
        if (request.amount() != null) transaction.setAmount(request.amount());
        if (request.originAccountId() != null) transaction.setOriginAccountId(request.originAccountId());
        if (request.destinationAccountId() != null) transaction.setDestinationAccountId(request.destinationAccountId());
        if (request.categoryId() != null) transaction.setCategoryId(request.categoryId());

        TransactionDTOS.TransactionContext context = null;

        switch (transaction.getType()) {
            case INGRESO -> context = applyIngreso(transaction, userId);
            case GASTO -> context = applyGasto(transaction, userId);
            case TRANSFERENCIA -> context = applyTransferencia(transaction, userId);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return TransactionDTOS.Response.builder()
                .id(savedTransaction.getId())
                .dateIssued(savedTransaction.getDateIssued())
                .type(savedTransaction.getType())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .originAccountId(savedTransaction.getOriginAccountId())
                .originAccountName(context.origin() != null ? context.origin().getName() : null)
                .destinationAccountId(savedTransaction.getDestinationAccountId())
                .destinationAccountName(context.destination() != null ? context.destination().getName() : null)
                .resultedInNegativeBalance(context.origin() != null && context.origin().getBalance() < 0)
                .categoryId(savedTransaction.getCategoryId())
                .categoryName(context.category() != null ? context.category().getName() : null)
                .build();
    }

    @Transactional
    public void deleteTransaction(
            UUID userId,
            UUID transactionId
    ) {
        Transaction transaction = transactionRepository.findEntityByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or does not belong to user"));

        revertTransaction(transaction, userId);
        transactionRepository.delete(transaction);
    }

    private void revertTransaction(@NonNull Transaction transaction, UUID userId) {
        switch (transaction.getType()) {
            case INGRESO -> revertIngreso(transaction, userId);
            case GASTO -> revertGasto(transaction, userId);
            case TRANSFERENCIA -> revertTransferencia(transaction, userId);
        }
    }

    private void revertIngreso (@NonNull Transaction transactionIngreso, UUID userId){
        Account destinationAccount = accountRepository.findByIdAndUserId(transactionIngreso.getDestinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        destinationAccount.setBalance(destinationAccount.getBalance() - transactionIngreso.getAmount());
        accountRepository.save(destinationAccount);
    }

    private void revertGasto (@NonNull Transaction transactionGasto, UUID userId){
        Account originAccount = accountRepository.findByIdAndUserId(transactionGasto.getOriginAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        originAccount.setBalance(originAccount.getBalance() + transactionGasto.getAmount());
        accountRepository.save(originAccount);
    }

    private void revertTransferencia (@NonNull Transaction transactionTransferencia, UUID userId){
        Account originAccount = accountRepository.findByIdAndUserId(transactionTransferencia.getOriginAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        Account destinationAccount = accountRepository.findByIdAndUserId(transactionTransferencia.getDestinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        originAccount.setBalance(originAccount.getBalance() + transactionTransferencia.getAmount());
        destinationAccount.setBalance(destinationAccount.getBalance() - transactionTransferencia.getAmount());

        accountRepository.save(originAccount);
        accountRepository.save(destinationAccount);
    }

    private TransactionDTOS.TransactionContext applyIngreso (@NonNull Transaction transactionIngreso, UUID userId) {
        if (transactionIngreso.getOriginAccountId() != null || transactionIngreso.getDestinationAccountId() == null) {
            throw new InvalidInputException("Destination Account ID is Null or payload includes Origin Account ID");
        }
        Category category = categoryRepository.findByIdAndUserIdAndType(
                        transactionIngreso.getCategoryId(), userId, CategoryType.INGRESO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category doesn't exist, does not belong to user, or is not of type INGRESO"));

        Account destinationAccount = accountRepository.findByIdAndUserId(transactionIngreso.getDestinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        destinationAccount.setBalance(destinationAccount.getBalance() + transactionIngreso.getAmount());
        return TransactionDTOS.TransactionContext.builder()
                .destination(accountRepository.save(destinationAccount))
                .category(category)
                .build();
    }

    private TransactionDTOS.TransactionContext applyGasto (@NonNull Transaction transactionGasto, UUID userId) {
        if (transactionGasto.getOriginAccountId() == null || transactionGasto.getDestinationAccountId() != null) {
            throw new InvalidInputException("Origin Account ID is Null or payload includes Destination Account ID");
        }
        Category category = categoryRepository.findByIdAndUserIdAndType(
                        transactionGasto.getCategoryId(), userId, CategoryType.GASTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category doesn't exist, does not belong to user, or is not of type GASTO"));

        Account originAccount = accountRepository.findByIdAndUserId(transactionGasto.getOriginAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        originAccount.setBalance(originAccount.getBalance() - transactionGasto.getAmount());
        return TransactionDTOS.TransactionContext.builder()
                .origin(accountRepository.save(originAccount))
                .category(category)
                .build();
    }

    private TransactionDTOS.TransactionContext applyTransferencia (@NonNull Transaction transactionTransferencia, UUID userId) {
        if (transactionTransferencia.getOriginAccountId() == null || transactionTransferencia.getDestinationAccountId() == null) {
            throw new InvalidInputException("Origin Account ID or Destination Account ID are Null");
        }
        Account originAccount = accountRepository.findByIdAndUserId(transactionTransferencia.getOriginAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        Account destinationAccount = accountRepository.findByIdAndUserId(transactionTransferencia.getDestinationAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account doesn't exist or does not belong to user"));

        originAccount.setBalance(originAccount.getBalance() - transactionTransferencia.getAmount());
        destinationAccount.setBalance(destinationAccount.getBalance() + transactionTransferencia.getAmount());

        return TransactionDTOS.TransactionContext.builder()
                .origin(accountRepository.save(originAccount))
                .destination(accountRepository.save(destinationAccount))
                .build();
    }
}
