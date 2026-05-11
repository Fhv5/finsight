package io.github.fhv5.finsight.dto;

import jakarta.validation.constraints.NotEmpty;

public class AuthDTOS {
    public record LoginRequest(
            @NotEmpty String email,
            @NotEmpty String password
    ) {}

    public record LoginResponse(String token) {}

    public record RegisterRequest(
            @NotEmpty String email,
            @NotEmpty String password,
            @NotEmpty String confirmPassword
    ) {}
}
