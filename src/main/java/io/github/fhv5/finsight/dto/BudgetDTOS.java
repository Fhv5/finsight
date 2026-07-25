package io.github.fhv5.finsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "Container for Budget Data Transfer Objects")
public class BudgetDTOS {
    @Builder
    @Schema(description = "Budget response representation")
    public record Response(
        @Schema(description = "Unique identifier of the budget", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,
        @Schema(description = "Maximum budget limit amount", example = "50000")
        Long limitAmount,
        @Schema(description = "ID of the associated category", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID categoryId,
        @Schema(description = "Total accumulated expense for this budget", example = "12500")
        Long accumulatedExpense
    ) {}

    @Schema(description = "Request payload to create a budget")
    public record CreateRequest(
        @Schema(description = "Budget limit amount", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @PositiveOrZero Long limitAmount,
        @Schema(description = "ID of the category for this budget", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID categoryId
    ) {}

    @Schema(description = "Request payload to update a budget")
    public record UpdateRequest(
        @Schema(description = "Updated budget limit amount", example = "75000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @PositiveOrZero Long limitAmount
    ) {}
}
