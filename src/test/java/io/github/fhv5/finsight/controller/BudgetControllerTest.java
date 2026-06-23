package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.BudgetDTOS;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.BudgetService;
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
class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private BudgetController budgetController;

    private static final String TIMEZONE = "America/New_York";

    private SecurityUser securityUser;
    private UUID userId;
    private UUID budgetId;
    private BudgetDTOS.Response mockResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        budgetId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        securityUser = new SecurityUser(user);

        mockResponse = BudgetDTOS.Response.builder()
                .id(budgetId)
                .limitAmount(5000L)
                .categoryId(UUID.randomUUID())
                .accumulatedExpense(1200L)
                .build();
    }

    @Test
    void getBudgetsForCurrentUser_ShouldReturnOkStatusAndList() {
        List<BudgetDTOS.Response> expected = List.of(mockResponse);
        when(budgetService.getBudgetsForCurrentUser(userId, TIMEZONE)).thenReturn(expected);

        ResponseEntity<List<BudgetDTOS.Response>> result =
                budgetController.getBudgetsForCurrentUser(securityUser, TIMEZONE);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expected, result.getBody());
        verify(budgetService).getBudgetsForCurrentUser(userId, TIMEZONE);
    }

    @Test
    void getBudgetById_ShouldReturnOkStatusAndBudget() {
        when(budgetService.getBudgetById(userId, budgetId, TIMEZONE)).thenReturn(mockResponse);

        ResponseEntity<BudgetDTOS.Response> result =
                budgetController.getBudgetById(securityUser, budgetId, TIMEZONE);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mockResponse, result.getBody());
        verify(budgetService).getBudgetById(userId, budgetId, TIMEZONE);
    }

    @Test
    void createBudget_ShouldReturnCreatedStatusAndBudget() {
        BudgetDTOS.CreateRequest request = new BudgetDTOS.CreateRequest(5000L, UUID.randomUUID());

        when(budgetService.createBudget(eq(userId), eq(TIMEZONE), any(BudgetDTOS.CreateRequest.class)))
                .thenReturn(mockResponse);

        ResponseEntity<BudgetDTOS.Response> result =
                budgetController.createBudget(securityUser, TIMEZONE, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(mockResponse, result.getBody());
        verify(budgetService).createBudget(eq(userId), eq(TIMEZONE), any(BudgetDTOS.CreateRequest.class));
    }

    @Test
    void updateBudget_ShouldReturnOkStatusAndUpdatedBudget() {
        BudgetDTOS.UpdateRequest request = new BudgetDTOS.UpdateRequest(10000L);

        BudgetDTOS.Response updatedResponse = BudgetDTOS.Response.builder()
                .id(budgetId)
                .limitAmount(10000L)
                .categoryId(mockResponse.categoryId())
                .accumulatedExpense(1200L)
                .build();

        when(budgetService.updateBudget(eq(userId), eq(budgetId), any(BudgetDTOS.UpdateRequest.class), eq(TIMEZONE)))
                .thenReturn(updatedResponse);

        ResponseEntity<BudgetDTOS.Response> result =
                budgetController.updateBudget(securityUser, budgetId, TIMEZONE, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(10000L, result.getBody().limitAmount());
        verify(budgetService).updateBudget(eq(userId), eq(budgetId), any(BudgetDTOS.UpdateRequest.class), eq(TIMEZONE));
    }

    @Test
    void deleteBudget_ShouldReturnNoContentStatus() {
        ResponseEntity<Void> result = budgetController.deleteBudget(securityUser, budgetId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(budgetService).deleteBudget(userId, budgetId);
    }
}
