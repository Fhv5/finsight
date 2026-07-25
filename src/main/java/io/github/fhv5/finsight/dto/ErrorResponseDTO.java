package io.github.fhv5.finsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Standardized error response payload")
public record ErrorResponseDTO(
        @Schema(description = "Timestamp when the error occurred", example = "2026-07-25T02:44:47Z")
        Instant timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP status description", example = "Bad Request")
        String error,
        @Schema(description = "Detailed error message", example = "Validation failed for request object")
        String message,
        @Schema(description = "Request URI path", example = "/accounts")
        String path,
        @Schema(description = "List of specific field validation errors, if applicable")
        List<FieldErrorDetail> fieldErrors
        ) {
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null);
    }

    public ErrorResponseDTO(int status, String error, String message, String path, List<FieldErrorDetail> fieldErrors) {
        this(Instant.now(), status, error, message, path, fieldErrors);
    }

    @Schema(description = "Validation field error detail")
    public record FieldErrorDetail(
            @Schema(description = "Name of the field with validation error", example = "name")
            String field,
            @Schema(description = "Validation error message", example = "must not be empty")
            String message
    ) {}
}
