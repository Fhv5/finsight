package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth", version = "1")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthDTOS.LoginResponse> register(@RequestBody AuthDTOS.RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTOS.LoginResponse> login(@RequestBody AuthDTOS.LoginRequest request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDTOS.LoginResponse> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody  AuthDTOS.RefreshRequest request) {
        return new ResponseEntity<>(authService.rotateTokens(authHeader, request.refreshToken()), HttpStatus.OK);
    }
}
