package io.github.fhv5.finsight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Container for Authentication Data Transfer Objects")
public class AuthDTOS {
    @Schema(description = "User login request")
    public record LoginRequest(
            @Schema(description = "User email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotEmpty String email,
            @Schema(description = "User password", example = "P@ssword123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotEmpty String password
    ) {}

    @Schema(description = "Authentication token response")
    public record LoginResponse(
            @Schema(description = "JWT Access Token used to authenticate requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            String accessToken,
            @Schema(description = "JWT Refresh Token used to obtain a new access token", example = "d9f8e7d6-c5b4-a3f2-e1d0-c9b8a7f6e5d4")
            String refreshToken
    ) {}

    @Schema(description = "User registration request")
    public record RegisterRequest(
            @Schema(description = "User email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotEmpty String email,
            @Schema(description = "User password", example = "P@ssword123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotEmpty String password,
            @Schema(description = "Password confirmation matching the password field", example = "P@ssword123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotEmpty String confirmPassword
    ) {}

    @Schema(description = "Token refresh request")
    public record RefreshRequest(
            @Schema(description = "JWT Refresh Token", example = "d9f8e7d6-c5b4-a3f2-e1d0-c9b8a7f6e5d4", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotEmpty String refreshToken
    ) {}
}
