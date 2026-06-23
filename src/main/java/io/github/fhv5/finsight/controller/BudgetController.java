package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.BudgetDTOS;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping(path = "/budgets", version = "1")
public class BudgetController {
    private final BudgetService budgetService;

    @Operation(
            summary = "Get all budgets",
            description = "Retrieves all budgets for the currently authenticated user, each with the current " +
                    "month's accumulated expense calculated using the provided timezone.",
            operationId = "getBudgetsForCurrentUser"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Budgets retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid timezone identifier"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed")
            }
    )
    @GetMapping
    public ResponseEntity<List<BudgetDTOS.Response>> getBudgetsForCurrentUser(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestHeader("X-User-Timezone") String userZoneId) {
        return new ResponseEntity<>(
                budgetService.getBudgetsForCurrentUser(securityUser.getId(), userZoneId),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Get budget by ID",
            description = "Retrieves a specific budget by its ID for the currently authenticated user, " +
                    "including the current month's accumulated expense.",
            operationId = "getBudgetById"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Budget retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid timezone identifier"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Budget not found or does not belong to user")
            }
    )
    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetDTOS.Response> getBudgetById(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID budgetId,
            @RequestHeader("X-User-Timezone") String userZoneId) {
        return new ResponseEntity<>(
                budgetService.getBudgetById(securityUser.getId(), budgetId, userZoneId),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Create a budget",
            description = "Creates a spending budget for a GASTO category. Only one budget per category is allowed.",
            operationId = "createBudget"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Budget created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data, invalid timezone, or category is not a GASTO type"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "409", description = "A budget for this category already exists")
            }
    )
    @PostMapping
    public ResponseEntity<BudgetDTOS.Response> createBudget(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestHeader("X-User-Timezone") String userZoneId,
            @Valid @RequestBody BudgetDTOS.CreateRequest request) {
        return new ResponseEntity<>(
                budgetService.createBudget(securityUser.getId(), userZoneId, request),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Update a budget",
            description = "Updates the spending limit of an existing budget.",
            operationId = "updateBudget"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Budget updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data or invalid timezone identifier"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Budget not found or does not belong to user")
            }
    )
    @PatchMapping("/{budgetId}")
    public ResponseEntity<BudgetDTOS.Response> updateBudget(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID budgetId,
            @RequestHeader("X-User-Timezone") String userZoneId,
            @Valid @RequestBody BudgetDTOS.UpdateRequest request) {
        return new ResponseEntity<>(
                budgetService.updateBudget(securityUser.getId(), budgetId, request, userZoneId),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Delete a budget",
            description = "Deletes a specific budget by its ID for the currently authenticated user.",
            operationId = "deleteBudget"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Budget not found or does not belong to user")
            }
    )
    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID budgetId) {
        budgetService.deleteBudget(securityUser.getId(), budgetId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

