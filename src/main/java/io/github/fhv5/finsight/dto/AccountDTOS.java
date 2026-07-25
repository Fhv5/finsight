package io.github.fhv5.finsight.dto;

import io.github.fhv5.finsight.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "Container for Account Data Transfer Objects")
public class AccountDTOS {
        @Builder
        @Schema(description = "Account response representation")
        public record Response(
                @Schema(description = "Unique identifier of the account", example = "123e4567-e89b-12d3-a456-426614174000")
                UUID id,
                @Schema(description = "Account name", example = "Cash")
                String name,
                @Schema(description = "Account description", example = "Main daily expenses account")
                String description,
                @Schema(description = "Current balance", example = "150000")
                Long balance,
                @Schema(description = "Type of account", example = "REGULAR")
                AccountType type,
                @Schema(description = "Savings goal target amount (only applicable for savings accounts)", example = "500000")
                Long targetAmount
        ) {}

        @Schema(description = "Request payload to create a standard account")
        public record CreateRequest(
                @Schema(description = "Account name", example = "Checking Account", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotEmpty String name,
                @Schema(description = "Account description", example = "Main daily expenses account", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotEmpty String description,
                @Schema(description = "Initial account balance", example = "100000", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull Long balance
        ) {}

        @Schema(description = "Request payload to create a savings account")
        public record CreateSavingsRequest(
                @Schema(description = "Savings account name", example = "Vacation Fund", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotEmpty String name,
                @Schema(description = "Savings goal description", example = "Savings for summer vacation", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotEmpty String description,
                @Schema(description = "Target amount to reach", example = "2000000", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull @Positive Long targetAmount
        ) {}

        @Schema(description = "Request payload to update an existing account")
        public record UpdateRequest(
                @Schema(description = "Updated account name", example = "Primary Checking")
                String name,
                @Schema(description = "Updated account description", example = "Updated main account description")
                String description
        ) {}
}
