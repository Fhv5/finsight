package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.repository.UserRepository;
import io.github.fhv5.finsight.security.JwtService;
import io.github.fhv5.finsight.security.SecurityUser;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthDTOS.LoginResponse register(AuthDTOS.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            // TODO: Change to custom exception and handle it in a global exception handler
            throw new IllegalArgumentException("Email already in use");
        }

        if (!request.password().equals(request.confirmPassword())) {
            // TODO: custom exception yea yea
            throw new IllegalArgumentException("Passwords do not match");
        }

        String encryptedPassword = passwordEncoder.encode(request.password());

        User newUser = User.builder()
                .email(request.email())
                .password(encryptedPassword)
                .build();

        User savedUser = userRepository.save(newUser);

        return tokenService.issueTokens(savedUser);
    }

    public AuthDTOS.LoginResponse login(AuthDTOS.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityUser secUser = (SecurityUser) authentication.getPrincipal();

        if (secUser == null) {
            throw new IllegalStateException("Authentication failed");
        }
        User user = secUser.user();


        return tokenService.issueTokens(user);
    }

    public AuthDTOS.LoginResponse rotateTokens(String authHeader, String refreshToken) {
        String jwt = authHeader.replace("Bearer ", "");

        try {
            jwtService.parseClaims(jwt).getId();
            throw new IllegalArgumentException("Access token is not expired yet");
        } catch (ExpiredJwtException e) {
            String jti = e.getClaims().getId();
            return tokenService.refreshTokens(jti, refreshToken);
        }
    }
}
