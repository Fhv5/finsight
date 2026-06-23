package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.TransactionDTOS;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.TransactionService;
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
@RequestMapping(path = "/transactions", version = "1")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(
            summary = "Get all transactions",
            description = "Retrieves a list of all transactions for the currently authenticated user.",
            operationId = "getTransactionsForCurrentUser"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed")
            }
    )
    @GetMapping
    public ResponseEntity<List<TransactionDTOS.Response>> getTransactionsForCurrentUser(
            @AuthenticationPrincipal SecurityUser securityUser) {
        return new ResponseEntity<>(
                transactionService.getTransactionsForCurrentUser(securityUser.getId()),
                HttpStatus.OK);
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Retrieves a specific transaction by its ID for the currently authenticated user.",
            operationId = "getTransactionById"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Transaction not found or does not belong to user")
            }
    )
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTOS.Response> getTransactionById(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID transactionId) {
        return new ResponseEntity<>(
                transactionService.getTransactionById(transactionId, securityUser.getId()),
                HttpStatus.OK);
    }

    @Operation(
            summary = "Create an income transaction (ingreso)",
            description = "Records an income transaction that adds the specified amount to the destination account.",
            operationId = "createIngreso"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Ingreso created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Destination account or INGRESO category not found")
            }
    )
    @PostMapping("/ingreso")
    public ResponseEntity<TransactionDTOS.Response> createIngreso(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody TransactionDTOS.CreateIngresoRequest request) {
        return new ResponseEntity<>(
                transactionService.createIngreso(securityUser.getId(), request),
                HttpStatus.CREATED);
    }

    @Operation(
            summary = "Create an expense transaction (gasto)",
            description = "Records an expense transaction that deducts the specified amount from the origin account.",
            operationId = "createGasto"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Gasto created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Origin account or GASTO category not found")
            }
    )
    @PostMapping("/gasto")
    public ResponseEntity<TransactionDTOS.Response> createGasto(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody TransactionDTOS.CreateGastoRequest request) {
        return new ResponseEntity<>(
                transactionService.createGasto(securityUser.getId(), request),
                HttpStatus.CREATED);
    }

    @Operation(
            summary = "Create a transfer transaction (transferencia)",
            description = "Records a transfer that moves the specified amount from the origin account to the destination account.",
            operationId = "createTransferencia"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Transferencia created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Origin or destination account not found")
            }
    )
    @PostMapping("/transferencia")
    public ResponseEntity<TransactionDTOS.Response> createTransferencia(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody TransactionDTOS.CreateTransferenciaRequest request) {
        return new ResponseEntity<>(
                transactionService.createTransferencia(securityUser.getId(), request),
                HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update a transaction",
            description = "Partially updates an existing transaction. The previous effect on account balances is " +
                    "reverted before the updated transaction is re-applied.",
            operationId = "updateTransaction"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Transaction, account, or category not found")
            }
    )
    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionDTOS.Response> updateTransaction(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody TransactionDTOS.UpdateRequest request,
            @PathVariable UUID transactionId) {
        return new ResponseEntity<>(
                transactionService.updateTransaction(securityUser.getId(), transactionId, request),
                HttpStatus.OK);
    }

    @Operation(
            summary = "Delete a transaction",
            description = "Deletes a transaction and reverts its effect on the associated account balances.",
            operationId = "deleteTransaction"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Transaction not found or does not belong to user")
            }
    )
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID transactionId) {
        transactionService.deleteTransaction(securityUser.getId(), transactionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
