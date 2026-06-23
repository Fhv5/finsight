package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.BudgetDTOS;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.Budget;
import io.github.fhv5.finsight.model.CategoryType;
import io.github.fhv5.finsight.repository.BudgetRepository;
import io.github.fhv5.finsight.repository.CategoryRepository;
import io.github.fhv5.finsight.repository.TransactionRepository;
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
class BudgetServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetService budgetService;

    private static final String VALID_TIMEZONE = "America/New_York";

    private UUID userId;
    private UUID budgetId;
    private UUID categoryId;
    private Budget mockBudget;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        budgetId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        mockBudget = Budget.builder()
                .id(budgetId)
                .limitAmount(5000L)
                .categoryId(categoryId)
                .userId(userId)
                .build();
    }

    // ─── getBudgetsForCurrentUser ──────────────────────────────────────────────

    @Test
    void getBudgetsForCurrentUser_ShouldReturnListWithAccumulatedExpense() {
        when(budgetRepository.findAllByUserId(userId)).thenReturn(List.of(mockBudget));
        when(transactionRepository.computeMonthlyExpenseByCategoryId(
                eq(categoryId), eq(userId), any(), any())).thenReturn(1200L);

        List<BudgetDTOS.Response> result = budgetService.getBudgetsForCurrentUser(userId, VALID_TIMEZONE);

        assertEquals(1, result.size());
        assertEquals(budgetId, result.getFirst().id());
        assertEquals(5000L, result.getFirst().limitAmount());
        assertEquals(1200L, result.getFirst().accumulatedExpense());
        verify(budgetRepository).findAllByUserId(userId);
    }

    @Test
    void getBudgetsForCurrentUser_ShouldReturnEmptyList_WhenNoBudgets() {
        when(budgetRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<BudgetDTOS.Response> result = budgetService.getBudgetsForCurrentUser(userId, VALID_TIMEZONE);

        assertTrue(result.isEmpty());
    }

    // ─── getBudgetById ─────────────────────────────────────────────────────────

    @Test
    void getBudgetById_ShouldReturnBudgetWithAccumulatedExpense_WhenValid() {
        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(mockBudget));
        when(transactionRepository.computeMonthlyExpenseByCategoryId(
                eq(categoryId), eq(userId), any(), any())).thenReturn(800L);

        BudgetDTOS.Response result = budgetService.getBudgetById(userId, budgetId, VALID_TIMEZONE);

        assertNotNull(result);
        assertEquals(budgetId, result.id());
        assertEquals(5000L, result.limitAmount());
        assertEquals(800L, result.accumulatedExpense());
    }

    @Test
    void getBudgetById_ShouldThrowException_WhenNotFound() {
        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> budgetService.getBudgetById(userId, budgetId, VALID_TIMEZONE));
    }

    // ─── createBudget ──────────────────────────────────────────────────────────

    @Test
    void createBudget_ShouldSaveAndReturnBudget_WhenValid() {
        BudgetDTOS.CreateRequest request = new BudgetDTOS.CreateRequest(5000L, categoryId);

        when(categoryRepository.existsByIdAndUserIdAndType(categoryId, userId, CategoryType.GASTO)).thenReturn(true);
        when(budgetRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            b.setId(budgetId);
            return b;
        });
        when(transactionRepository.computeMonthlyExpenseByCategoryId(
                eq(categoryId), eq(userId), any(), any())).thenReturn(0L);

        BudgetDTOS.Response result = budgetService.createBudget(userId, VALID_TIMEZONE, request);

        assertNotNull(result);
        assertEquals(budgetId, result.id());
        assertEquals(5000L, result.limitAmount());
        assertEquals(categoryId, result.categoryId());
        assertEquals(0L, result.accumulatedExpense());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldThrowException_WhenCategoryIsNotGasto() {
        BudgetDTOS.CreateRequest request = new BudgetDTOS.CreateRequest(5000L, categoryId);

        when(categoryRepository.existsByIdAndUserIdAndType(categoryId, userId, CategoryType.GASTO)).thenReturn(false);

        assertThrows(InvalidInputException.class,
                () -> budgetService.createBudget(userId, VALID_TIMEZONE, request));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldThrowException_WhenBudgetAlreadyExistsForCategory() {
        BudgetDTOS.CreateRequest request = new BudgetDTOS.CreateRequest(5000L, categoryId);

        when(categoryRepository.existsByIdAndUserIdAndType(categoryId, userId, CategoryType.GASTO)).thenReturn(true);
        when(budgetRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(true);

        assertThrows(InvalidInputException.class,
                () -> budgetService.createBudget(userId, VALID_TIMEZONE, request));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // ─── updateBudget ──────────────────────────────────────────────────────────

    @Test
    void updateBudget_ShouldUpdateLimitAndReturnBudget_WhenValid() {
        BudgetDTOS.UpdateRequest request = new BudgetDTOS.UpdateRequest(10000L);

        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(mockBudget));
        when(budgetRepository.save(mockBudget)).thenReturn(mockBudget);
        when(transactionRepository.computeMonthlyExpenseByCategoryId(
                eq(categoryId), eq(userId), any(), any())).thenReturn(3000L);

        BudgetDTOS.Response result = budgetService.updateBudget(userId, budgetId, request, VALID_TIMEZONE);

        assertNotNull(result);
        assertEquals(10000L, mockBudget.getLimitAmount());
        assertEquals(3000L, result.accumulatedExpense());
        verify(budgetRepository).save(mockBudget);
    }

    @Test
    void updateBudget_ShouldThrowException_WhenNotFound() {
        BudgetDTOS.UpdateRequest request = new BudgetDTOS.UpdateRequest(10000L);

        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> budgetService.updateBudget(userId, budgetId, request, VALID_TIMEZONE));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // ─── deleteBudget ──────────────────────────────────────────────────────────

    @Test
    void deleteBudget_ShouldDelete_WhenValid() {
        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.of(mockBudget));

        budgetService.deleteBudget(userId, budgetId);

        verify(budgetRepository).delete(mockBudget);
    }

    @Test
    void deleteBudget_ShouldThrowException_WhenNotFound() {
        when(budgetRepository.findByIdAndUserId(budgetId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> budgetService.deleteBudget(userId, budgetId));
        verify(budgetRepository, never()).delete(any(Budget.class));
    }
}
