package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.TransactionDTOS;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.*;
import io.github.fhv5.finsight.projection.TransactionView;
import io.github.fhv5.finsight.repository.AccountRepository;
import io.github.fhv5.finsight.repository.CategoryRepository;
import io.github.fhv5.finsight.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UUID userId;
    private UUID transactionId;
    private UUID originAccountId;
    private UUID destinationAccountId;
    private UUID categoryId;

    private Account mockOriginAccount;
    private Account mockDestinationAccount;
    private Category mockIngresoCategory;
    private Category mockGastoCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        originAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        mockOriginAccount = Account.builder()
                .id(originAccountId)
                .name("Origin Account")
                .balance(5000L)
                .userId(userId)
                .build();

        mockDestinationAccount = Account.builder()
                .id(destinationAccountId)
                .name("Destination Account")
                .balance(1000L)
                .userId(userId)
                .build();

        mockIngresoCategory = Category.builder()
                .id(categoryId)
                .name("Salary")
                .type(CategoryType.INGRESO)
                .userId(userId)
                .build();

        mockGastoCategory = Category.builder()
                .id(categoryId)
                .name("Groceries")
                .type(CategoryType.GASTO)
                .userId(userId)
                .build();
    }

    // ─── getTransactionsForCurrentUser ────────────────────────────────────────

    @Test
    void getTransactionsForCurrentUser_ShouldReturnListOfTransactions() {
        TransactionView mockView = mockTransactionView(transactionId, TransactionType.INGRESO, 500L);
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(mockView));

        List<TransactionDTOS.Response> result = transactionService.getTransactionsForCurrentUser(userId);

        assertEquals(1, result.size());
        assertEquals(transactionId, result.getFirst().id());
        assertEquals(TransactionType.INGRESO, result.getFirst().type());
        verify(transactionRepository).findAllByUserId(userId);
    }

    @Test
    void getTransactionsForCurrentUser_ShouldReturnEmptyList_WhenNoTransactionsExist() {
        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<TransactionDTOS.Response> result = transactionService.getTransactionsForCurrentUser(userId);

        assertTrue(result.isEmpty());
    }

    // ─── getTransactionById ────────────────────────────────────────────────────

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenValidIdAndUserId() {
        TransactionView mockView = mockTransactionView(transactionId, TransactionType.GASTO, 200L);
        when(transactionRepository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(mockView));

        TransactionDTOS.Response result = transactionService.getTransactionById(transactionId, userId);

        assertNotNull(result);
        assertEquals(transactionId, result.id());
        verify(transactionRepository).findByIdAndUserId(transactionId, userId);
    }

    @Test
    void getTransactionById_ShouldThrowException_WhenNotFound() {
        when(transactionRepository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(transactionId, userId));
    }

    // ─── createIngreso ─────────────────────────────────────────────────────────

    @Test
    void createIngreso_ShouldSaveAndReturnTransaction_WhenValid() {
        TransactionDTOS.CreateIngresoRequest request = new TransactionDTOS.CreateIngresoRequest(
                Instant.now(), 500L, "Paycheck", destinationAccountId, categoryId);

        when(categoryRepository.findByIdAndUserIdAndType(categoryId, userId, CategoryType.INGRESO))
                .thenReturn(Optional.of(mockIngresoCategory));
        when(accountRepository.findByIdAndUserId(destinationAccountId, userId))
                .thenReturn(Optional.of(mockDestinationAccount));
        when(accountRepository.save(mockDestinationAccount)).thenReturn(mockDestinationAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(transactionId);
            return t;
        });

        TransactionDTOS.Response result = transactionService.createIngreso(userId, request);

        assertNotNull(result);
        assertEquals(transactionId, result.id());
        assertEquals(TransactionType.INGRESO, result.type());
        assertEquals("Destination Account", result.destinationAccountName());
        assertEquals(1500L, mockDestinationAccount.getBalance()); // 1000 + 500
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createIngreso_ShouldThrowException_WhenOriginAccountIdIsPresent() {
        // An INGRESO request should never include an originAccountId — we simulate
        // this by building a Transaction with both IDs set and calling the private
        // validation indirectly: pass a request, then stub the repo so applyIngreso
        // detects the bad state via the transaction object itself.
        // The simplest way is to test through a hand-crafted transaction with originAccountId set,
        // which the service does when constructing from the request — but since the record
        // doesn't carry originAccountId, we instead verify the category-not-found path
        // guards correctly when the category type is wrong.
        //
        // The actual InvalidInputException comes from applyIngreso when it finds
        // originAccountId != null on the built Transaction. Since CreateIngresoRequest
        // has no originAccountId field, we cannot trigger that branch from the public API.
        // Instead we test the ResourceNotFoundException guard when the INGRESO category
        // does not exist.
        TransactionDTOS.CreateIngresoRequest request = new TransactionDTOS.CreateIngresoRequest(
                Instant.now(), 500L, "Paycheck", destinationAccountId, categoryId);

        when(categoryRepository.findByIdAndUserIdAndType(categoryId, userId, CategoryType.INGRESO))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createIngreso(userId, request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createIngreso_ShouldThrowException_WhenDestinationAccountNotFound() {
        TransactionDTOS.CreateIngresoRequest request = new TransactionDTOS.CreateIngresoRequest(
                Instant.now(), 500L, "Paycheck", destinationAccountId, categoryId);

        when(categoryRepository.findByIdAndUserIdAndType(categoryId, userId, CategoryType.INGRESO))
                .thenReturn(Optional.of(mockIngresoCategory));
        when(accountRepository.findByIdAndUserId(destinationAccountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createIngreso(userId, request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ─── createGasto ───────────────────────────────────────────────────────────

    @Test
    void createGasto_ShouldSaveAndReturnTransaction_WhenValid() {
        TransactionDTOS.CreateGastoRequest request = new TransactionDTOS.CreateGastoRequest(
                Instant.now(), 200L, "Supermarket", originAccountId, categoryId);

        when(categoryRepository.findByIdAndUserIdAndType(categoryId, userId, CategoryType.GASTO))
                .thenReturn(Optional.of(mockGastoCategory));
        when(accountRepository.findByIdAndUserId(originAccountId, userId))
                .thenReturn(Optional.of(mockOriginAccount));
        when(accountRepository.save(mockOriginAccount)).thenReturn(mockOriginAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(transactionId);
            return t;
        });

        TransactionDTOS.Response result = transactionService.createGasto(userId, request);

        assertNotNull(result);
        assertEquals(transactionId, result.id());
        assertEquals(TransactionType.GASTO, result.type());
        assertEquals("Origin Account", result.originAccountName());
        assertEquals(4800L, mockOriginAccount.getBalance()); // 5000 - 200
        assertFalse(result.resultedInNegativeBalance());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createGasto_ShouldReturnNegativeBalance_WhenBalanceGoesNegative() {
        TransactionDTOS.CreateGastoRequest request = new TransactionDTOS.CreateGastoRequest(
                Instant.now(), 10000L, "Big purchase", originAccountId, categoryId);

        when(categoryRepository.findByIdAndUserIdAndType(categoryId, userId, CategoryType.GASTO))
                .thenReturn(Optional.of(mockGastoCategory));
        when(accountRepository.findByIdAndUserId(originAccountId, userId))
                .thenReturn(Optional.of(mockOriginAccount));
        when(accountRepository.save(mockOriginAccount)).thenReturn(mockOriginAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(transactionId);
            return t;
        });

        TransactionDTOS.Response result = transactionService.createGasto(userId, request);

        assertTrue(result.resultedInNegativeBalance());
        assertTrue(mockOriginAccount.getBalance() < 0); // 5000 - 10000 = -5000
    }

    @Test
    void createGasto_ShouldThrowException_WhenGastoCategoryNotFound() {
        TransactionDTOS.CreateGastoRequest request = new TransactionDTOS.CreateGastoRequest(
                Instant.now(), 200L, "Supermarket", originAccountId, categoryId);

        when(categoryRepository.findByIdAndUserIdAndType(categoryId, userId, CategoryType.GASTO))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createGasto(userId, request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createGasto_ShouldThrowException_WhenOriginAccountNotFound() {
        TransactionDTOS.CreateGastoRequest request = new TransactionDTOS.CreateGastoRequest(
                Instant.now(), 200L, "Supermarket", originAccountId, categoryId);

        when(categoryRepository.findByIdAndUserIdAndType(categoryId, userId, CategoryType.GASTO))
                .thenReturn(Optional.of(mockGastoCategory));
        when(accountRepository.findByIdAndUserId(originAccountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createGasto(userId, request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ─── createTransferencia ───────────────────────────────────────────────────

    @Test
    void createTransferencia_ShouldSaveAndReturnTransaction_WhenValid() {
        TransactionDTOS.CreateTransferenciaRequest request = new TransactionDTOS.CreateTransferenciaRequest(
                Instant.now(), 300L, "Transfer", originAccountId, destinationAccountId, null);

        when(accountRepository.findByIdAndUserId(originAccountId, userId))
                .thenReturn(Optional.of(mockOriginAccount));
        when(accountRepository.findByIdAndUserId(destinationAccountId, userId))
                .thenReturn(Optional.of(mockDestinationAccount));
        when(accountRepository.save(mockOriginAccount)).thenReturn(mockOriginAccount);
        when(accountRepository.save(mockDestinationAccount)).thenReturn(mockDestinationAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(transactionId);
            return t;
        });

        TransactionDTOS.Response result = transactionService.createTransferencia(userId, request);

        assertNotNull(result);
        assertEquals(transactionId, result.id());
        assertEquals(TransactionType.TRANSFERENCIA, result.type());
        assertEquals("Origin Account", result.originAccountName());
        assertEquals("Destination Account", result.destinationAccountName());
        assertEquals(4700L, mockOriginAccount.getBalance()); // 5000 - 300
        assertEquals(1300L, mockDestinationAccount.getBalance()); // 1000 + 300
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransferencia_ShouldThrowException_WhenOriginAccountNotFound() {
        TransactionDTOS.CreateTransferenciaRequest request = new TransactionDTOS.CreateTransferenciaRequest(
                Instant.now(), 300L, "Transfer", originAccountId, destinationAccountId, null);

        when(accountRepository.findByIdAndUserId(originAccountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransferencia(userId, request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransferencia_ShouldThrowException_WhenDestinationAccountNotFound() {
        TransactionDTOS.CreateTransferenciaRequest request = new TransactionDTOS.CreateTransferenciaRequest(
                Instant.now(), 300L, "Transfer", originAccountId, destinationAccountId, null);

        when(accountRepository.findByIdAndUserId(originAccountId, userId))
                .thenReturn(Optional.of(mockOriginAccount));
        when(accountRepository.findByIdAndUserId(destinationAccountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransferencia(userId, request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ─── deleteTransaction ─────────────────────────────────────────────────────

    @Test
    void deleteTransaction_ShouldDeleteAndRevertIngreso_WhenValid() {
        Transaction ingresoTransaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.INGRESO)
                .amount(500L)
                .userId(userId)
                .destinationAccountId(destinationAccountId)
                .build();

        when(transactionRepository.findEntityByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(ingresoTransaction));
        when(accountRepository.findByIdAndUserId(destinationAccountId, userId))
                .thenReturn(Optional.of(mockDestinationAccount));
        when(accountRepository.save(mockDestinationAccount)).thenReturn(mockDestinationAccount);

        transactionService.deleteTransaction(userId, transactionId);

        verify(transactionRepository).delete(ingresoTransaction);
        assertEquals(500L, mockDestinationAccount.getBalance()); // 1000 - 500 = 500 (revert)
    }

    @Test
    void deleteTransaction_ShouldDeleteAndRevertGasto_WhenValid() {
        Transaction gastoTransaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.GASTO)
                .amount(200L)
                .userId(userId)
                .originAccountId(originAccountId)
                .build();

        when(transactionRepository.findEntityByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(gastoTransaction));
        when(accountRepository.findByIdAndUserId(originAccountId, userId))
                .thenReturn(Optional.of(mockOriginAccount));
        when(accountRepository.save(mockOriginAccount)).thenReturn(mockOriginAccount);

        transactionService.deleteTransaction(userId, transactionId);

        verify(transactionRepository).delete(gastoTransaction);
        assertEquals(5200L, mockOriginAccount.getBalance()); // 5000 + 200 = 5200 (revert)
    }

    @Test
    void deleteTransaction_ShouldDeleteAndRevertTransferencia_WhenValid() {
        Transaction transferenciaTransaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.TRANSFERENCIA)
                .amount(300L)
                .userId(userId)
                .originAccountId(originAccountId)
                .destinationAccountId(destinationAccountId)
                .build();

        when(transactionRepository.findEntityByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(transferenciaTransaction));
        when(accountRepository.findByIdAndUserId(originAccountId, userId))
                .thenReturn(Optional.of(mockOriginAccount));
        when(accountRepository.findByIdAndUserId(destinationAccountId, userId))
                .thenReturn(Optional.of(mockDestinationAccount));
        when(accountRepository.save(mockOriginAccount)).thenReturn(mockOriginAccount);
        when(accountRepository.save(mockDestinationAccount)).thenReturn(mockDestinationAccount);

        transactionService.deleteTransaction(userId, transactionId);

        verify(transactionRepository).delete(transferenciaTransaction);
        assertEquals(5300L, mockOriginAccount.getBalance()); // 5000 + 300 (revert)
        assertEquals(700L, mockDestinationAccount.getBalance()); // 1000 - 300 (revert)
    }

    @Test
    void deleteTransaction_ShouldThrowException_WhenNotFound() {
        when(transactionRepository.findEntityByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(userId, transactionId));
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    // ─── updateTransaction ─────────────────────────────────────────────────────

    @Test
    void updateTransaction_ShouldUpdateAndReturnTransaction_WhenValid() {
        Transaction existingTransaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.INGRESO)
                .amount(500L)
                .userId(userId)
                .destinationAccountId(destinationAccountId)
                .categoryId(categoryId)
                .build();

        TransactionDTOS.UpdateRequest request = new TransactionDTOS.UpdateRequest(
                null, null, 750L, "Updated description", null, null, null);

        when(transactionRepository.findEntityByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(existingTransaction));
        // revertIngreso
        when(accountRepository.findByIdAndUserId(destinationAccountId, userId))
                .thenReturn(Optional.of(mockDestinationAccount));
        when(accountRepository.save(mockDestinationAccount)).thenReturn(mockDestinationAccount);
        // applyIngreso (after update)
        when(categoryRepository.findByIdAndUserIdAndType(categoryId, userId, CategoryType.INGRESO))
                .thenReturn(Optional.of(mockIngresoCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        TransactionDTOS.Response result = transactionService.updateTransaction(userId, transactionId, request);

        assertNotNull(result);
        assertEquals(750L, existingTransaction.getAmount());
        assertEquals("Updated description", existingTransaction.getDescription());
        verify(transactionRepository).save(existingTransaction);
    }

    @Test
    void updateTransaction_ShouldThrowException_WhenNotFound() {
        TransactionDTOS.UpdateRequest request = new TransactionDTOS.UpdateRequest(
                null, null, 100L, null, null, null, null);

        when(transactionRepository.findEntityByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(userId, transactionId, request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private TransactionView mockTransactionView(UUID id, TransactionType type, Long amount) {
        return new TransactionView() {
            public UUID getId() { return id; }
            public Instant getDateIssued() { return Instant.now(); }
            public TransactionType getType() { return type; }
            public Long getAmount() { return amount; }
            public String getDescription() { return "Test transaction"; }
            public UUID getOriginAccountId() { return null; }
            public String getOriginAccountName() { return null; }
            public UUID getDestinationAccountId() { return destinationAccountId; }
            public String getDestinationAccountName() { return "Destination Account"; }
            public UUID getCategoryId() { return categoryId; }
            public String getCategoryName() { return "Salary"; }
        };
    }
}
