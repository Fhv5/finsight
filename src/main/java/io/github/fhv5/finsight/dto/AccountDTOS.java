package io.github.fhv5.finsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

public class AccountDTOS {
        @Builder
        @Schema(description = "Response object containing account details")
        public record Response(
                @Schema(description = "Unique identifier of the account", example = "123e4567-e89b-12d3-a456-426614174000")
                UUID id,
                @Schema(description = "Name of the account", example = "Main Checking")
                String name,
                @Schema(description = "Description of the account", example = "Everyday expenses")
                String description,
                @Schema(description = "Current balance in cents", example = "150000")
                Long balance
        ) {}

        @Schema(description = "Request object for creating a new account")
        public record CreateRequest(
                @NotEmpty 
                @Schema(description = "Name of the account", example = "Main Checking", requiredMode = Schema.RequiredMode.REQUIRED)
                String name,
                @NotEmpty 
                @Schema(description = "Description of the account", example = "Everyday expenses", requiredMode = Schema.RequiredMode.REQUIRED)
                String description,
                @NotNull 
                @Schema(description = "Initial balance in cents", example = "150000", requiredMode = Schema.RequiredMode.REQUIRED)
                Long balance
        ) {}

        @Schema(description = "Request object for updating an existing account")
        public record UpdateRequest(
                @Schema(description = "New name of the account", example = "Updated Checking")
                String name,
                @Schema(description = "New description of the account", example = "Updated everyday expenses")
                String description
        ) {}
}
