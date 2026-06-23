package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.BudgetDTOS;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.Budget;
import io.github.fhv5.finsight.model.CategoryType;
import io.github.fhv5.finsight.repository.BudgetRepository;
import io.github.fhv5.finsight.repository.CategoryRepository;
import io.github.fhv5.finsight.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BudgetService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public List<BudgetDTOS.Response> getBudgetsForCurrentUser(UUID userId, String userZoneId) {
        List<Budget> budgets = budgetRepository.findAllByUserId(userId);

        return budgets.stream()
                .map(budget -> BudgetDTOS.Response.builder()
                        .id(budget.getId())
                        .categoryId(budget.getCategoryId())
                        .limitAmount(budget.getLimitAmount())
                        .accumulatedExpense(getAccumulatedExpense(userId, userZoneId, budget))
                        .build())
                .toList();
    }

    public BudgetDTOS.Response getBudgetById(UUID userId, UUID budgetId, String userZoneId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found or does not belong to user"));

        Long accumulatedExpense = getAccumulatedExpense(userId, userZoneId, budget);

        return BudgetDTOS.Response.builder()
                .id(budget.getId())
                .categoryId(budget.getCategoryId())
                .limitAmount(budget.getLimitAmount())
                .accumulatedExpense(accumulatedExpense)
                .build();
    }

    public BudgetDTOS.Response createBudget(UUID userId, String userZoneId, BudgetDTOS.CreateRequest request) {
        if (!categoryRepository.existsByIdAndUserIdAndType(request.categoryId(), userId,CategoryType.GASTO)) {
            throw new InvalidInputException("A GASTO category with this ID does not exist");
        }
        if (budgetRepository.existsByCategoryIdAndUserId(request.categoryId(), userId)) {
            throw new InvalidInputException("A budget for this category already exists");
        }

        Budget newBudget = Budget.builder()
                .limitAmount(request.limitAmount())
                .categoryId(request.categoryId())
                .userId(userId)
                .build();

        Budget savedBudget = budgetRepository.save(newBudget);

        return BudgetDTOS.Response.builder()
                .id(savedBudget.getId())
                .categoryId(savedBudget.getCategoryId())
                .limitAmount(savedBudget.getLimitAmount())
                .accumulatedExpense(getAccumulatedExpense(userId, userZoneId, savedBudget))
                .build();
    }

    public BudgetDTOS.Response updateBudget(UUID userId, UUID budgetId, BudgetDTOS.UpdateRequest request, String userZoneId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found or does not belong to user"));

        budget.setLimitAmount(request.limitAmount());

        Budget savedBudget = budgetRepository.save(budget);

        return BudgetDTOS.Response.builder()
                .id(savedBudget.getId())
                .categoryId(savedBudget.getCategoryId())
                .limitAmount(savedBudget.getLimitAmount())
                .accumulatedExpense(getAccumulatedExpense(userId, userZoneId, savedBudget))
                .build();
    }

    public void deleteBudget(UUID userId, UUID budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found or does not belong to user"));

        budgetRepository.delete(budget);
    }

    private Long getAccumulatedExpense(UUID userId, String userZoneId, Budget budget) {
        ZoneId zone = ZoneId.of(userZoneId);
        YearMonth currentUserMonth =YearMonth .now(zone);

        Instant start = currentUserMonth
                .atDay(1)
                .atStartOfDay(zone)
                .toInstant();

        Instant end = currentUserMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(zone)
                .toInstant();

        return  transactionRepository.computeMonthlyExpenseByCategoryId(budget.getCategoryId(), userId, start, end);
    }
}
