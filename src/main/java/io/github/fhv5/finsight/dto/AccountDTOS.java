package io.github.fhv5.finsight.dto;

import io.github.fhv5.finsight.model.AccountType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.UUID;

public class AccountDTOS {
        @Builder
        public record Response(
                UUID id,
                String name,
                String description,
                Long balance,
                AccountType type,
                Long targetAmount
        ) {}

        public record CreateRequest(
                @NotEmpty String name,
                @NotEmpty String description,
                @NotNull Long balance
        ) {}

        public record CreateSavingsRequest(
                @NotEmpty String name,
                @NotEmpty String description,
                @NotNull @Positive Long targetAmount
        ) {}

        public record UpdateRequest(
                String name,
                String description
        ) {}
}
