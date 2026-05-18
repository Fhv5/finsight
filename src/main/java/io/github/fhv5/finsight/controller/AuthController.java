package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/auth", version = "1")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided email and password. " +
                    "The password must be confirmed by providing the same value in the confirmPassword field.",
            operationId = "registerUser"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data or password mismatch"),
                    @ApiResponse(responseCode = "409", description = "Email already in use")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<AuthDTOS.LoginResponse> register(@RequestBody AuthDTOS.RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Authenticate a user and issue tokens",
            description = "Authenticates the user with the provided email and password. " +
                    "If authentication is successful, an access token and a refresh token are issued.",
            operationId = "loginUser"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthDTOS.LoginResponse> login(@RequestBody AuthDTOS.LoginRequest request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }

    @Operation(
            summary = "Refresh access token using a refresh token",
            description = "Rotates the access and refresh tokens. The client must provide the current access token in the Authorization header and the refresh token in the request body. " +
                    "If the refresh token is valid and not expired, new tokens are issued.",
            operationId = "refreshTokens"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthDTOS.LoginResponse> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody AuthDTOS.RefreshRequest request) {
        return new ResponseEntity<>(authService.rotateTokens(authHeader, request.refreshToken()), HttpStatus.OK);
    }
}
