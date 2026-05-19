package io.github.fhv5.finsight.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

public class AccountDTOS {
        @Builder
        public record Response(
                UUID id,
                String name,
                String description,
                Long balance
        ) {}

        public record CreateRequest(
                @NotEmpty String name,
                @NotEmpty String description,
                @NotNull Long balance
        ) {}

        public record UpdateRequest(
                String name,
                String description
        ) {}
}
