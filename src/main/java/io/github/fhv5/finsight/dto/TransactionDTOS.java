package io.github.fhv5.finsight.dto;

import io.github.fhv5.finsight.model.Account;
import io.github.fhv5.finsight.model.Category;
import io.github.fhv5.finsight.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Container for Transaction Data Transfer Objects")
public class TransactionDTOS {
    @Builder
    @Schema(description = "Transaction response representation")
    public record Response(
            @Schema(description = "Unique identifier of the transaction", example = "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f")
            UUID id,
            @Schema(description = "Date and time when the transaction occurred", example = "2026-07-25T10:00:00Z")
            Instant dateIssued,
            @Schema(description = "Type of transaction", example = "GASTO")
            TransactionType type,
            @Schema(description = "Transaction amount", example = "4500")
            Long amount,
            @Schema(description = "Transaction description", example = "Supermarket grocery shopping")
            String description,
            @Schema(description = "ID of the origin account", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID originAccountId,
            @Schema(description = "Name of the origin account", example = "Checking Account")
            String originAccountName,
            @Schema(description = "ID of the destination account", example = "987e6543-e21b-12d3-a456-426614174999")
            UUID destinationAccountId,
            @Schema(description = "Name of the destination account", example = "Savings Account")
            String destinationAccountName,
            @Schema(description = "ID of the category", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
            UUID categoryId,
            @Schema(description = "Name of the category", example = "Groceries")
            String categoryName,
            @Schema(description = "Flag indicating if this transaction caused a negative account balance", example = "false")
            boolean resultedInNegativeBalance
    ) {}

    @Schema(description = "Request payload to create an expense transaction (GASTO)")
    public record CreateGastoRequest(
            @Schema(description = "Date and time of the transaction", example = "2026-07-25T10:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull Instant dateIssued,
            @Schema(description = "Expense amount", example = "4500", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull Long amount,
            @Schema(description = "Expense description", example = "Supermarket grocery shopping", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank String description,
            @Schema(description = "ID of the source account", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull UUID originAccountId,
            @Schema(description = "ID of the category", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull UUID categoryId
    ) {}

    @Schema(description = "Request payload to create an income transaction (INGRESO)")
    public record CreateIngresoRequest(
            @Schema(description = "Date and time of the transaction", example = "2026-07-25T10:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull Instant dateIssued,
            @Schema(description = "Income amount", example = "250000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull Long amount,
            @Schema(description = "Income description", example = "Monthly Salary", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank String description,
            @Schema(description = "ID of the target account", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull UUID destinationAccountId,
            @Schema(description = "ID of the category", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull UUID categoryId
    ) {}

    @Schema(description = "Request payload to create a transfer transaction (TRANSFERENCIA)")
    public record CreateTransferenciaRequest(
            @Schema(description = "Date and time of the transfer", example = "2026-07-25T10:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull Instant dateIssued,
            @Schema(description = "Transfer amount", example = "10000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull Long amount,
            @Schema(description = "Transfer description", example = "Monthly savings transfer", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank String description,
            @Schema(description = "ID of the source account", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull UUID originAccountId,
            @Schema(description = "ID of the destination account", example = "987e6543-e21b-12d3-a456-426614174999", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull UUID destinationAccountId,
            @Schema(description = "Optional ID of the category", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
            UUID categoryId
    ) {}

    @Schema(description = "Request payload to update a transaction")
    public record UpdateRequest(
            @Schema(description = "Updated date and time", example = "2026-07-25T10:00:00Z")
            Instant dateIssued,
            @Schema(description = "Updated transaction type", example = "GASTO")
            TransactionType type,
            @Schema(description = "Updated transaction amount", example = "5000")
            Long amount,
            @Schema(description = "Updated transaction description", example = "Updated grocery shopping")
            String description,
            @Schema(description = "Updated origin account ID", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID originAccountId,
            @Schema(description = "Updated destination account ID", example = "987e6543-e21b-12d3-a456-426614174999")
            UUID destinationAccountId,
            @Schema(description = "Updated category ID", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
            UUID categoryId
    ) {}

    @Builder
    @Schema(description = "Internal transaction context containing resolved entities")
    public record TransactionContext(
            @Schema(description = "Resolved origin account entity")
            Account origin,
            @Schema(description = "Resolved destination account entity")
            Account destination,
            @Schema(description = "Resolved category entity")
            Category category
    ) {}
}
