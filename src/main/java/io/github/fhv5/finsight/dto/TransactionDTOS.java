package io.github.fhv5.finsight.dto;

import io.github.fhv5.finsight.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class TransactionDTOS {
    @Builder
    public record Response(
            UUID id,
            Instant dateIssued,
            TransactionType type,
            Long amount,
            String description,
            UUID originAccountId,
            String originAccountName,
            UUID destinationAccountId,
            String destinationAccountName,
            UUID categoryId,
            String categoryName,
            boolean resultedInNegativeBalance
    ) {}

    public record CreateGastoRequest(
            @NotNull Instant dateIssued,
            @NotNull Long amount,
            @NotBlank String description,
            @NotNull UUID originAccountId,
            @NotNull UUID categoryId
    ) {}

    public record CreateIngresoRequest(
            @NotNull Instant dateIssued,
            @NotNull Long amount,
            @NotBlank String description,
            @NotNull UUID destinationAccountId,
            @NotNull UUID categoryId
    ) {}

    public record CreateTransferenciaRequest(
            @NotNull Instant dateIssued,
            @NotNull Long amount,
            @NotBlank String description,
            @NotNull UUID originAccountId,
            @NotNull UUID destinationAccountId,
            UUID categoryId
    ) {}
}
