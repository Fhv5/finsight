package io.github.fhv5.finsight.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.util.UUID;

public class BudgetDTOS {
    @Builder
    public record Response(
        UUID id,
        Long limitAmount,
        UUID categoryId,
        Long accumulatedExpense
    ) {}

    public record CreateRequest(
        @NotNull @PositiveOrZero Long limitAmount,
        @NotNull UUID categoryId
    ) {}

    public record UpdateRequest(
        @NotNull @PositiveOrZero Long limitAmount
    ) {}
}
