package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_ShouldReturnCreatedStatusAndTokens() {
        AuthDTOS.RegisterRequest request = new AuthDTOS.RegisterRequest("test@test.com", "password", "password");
        AuthDTOS.LoginResponse response = new AuthDTOS.LoginResponse("accessToken", "refreshToken");

        when(authService.register(any(AuthDTOS.RegisterRequest.class))).thenReturn(response);

        ResponseEntity<AuthDTOS.LoginResponse> result = authController.register(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void login_ShouldReturnOkStatusAndTokens() {
        AuthDTOS.LoginRequest request = new AuthDTOS.LoginRequest("test@test.com", "password");
        AuthDTOS.LoginResponse response = new AuthDTOS.LoginResponse("accessToken", "refreshToken");

        when(authService.login(any(AuthDTOS.LoginRequest.class))).thenReturn(response);

        ResponseEntity<AuthDTOS.LoginResponse> result = authController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void refreshToken_ShouldReturnOkStatusAndTokens() {
        String authHeader = "Bearer oldAccessToken";
        AuthDTOS.RefreshRequest request = new AuthDTOS.RefreshRequest("oldRefreshToken");
        AuthDTOS.LoginResponse response = new AuthDTOS.LoginResponse("newAccessToken", "newRefreshToken");

        when(authService.rotateTokens(eq(authHeader), eq(request.refreshToken()))).thenReturn(response);

        ResponseEntity<AuthDTOS.LoginResponse> result = authController.refreshToken(authHeader, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}
