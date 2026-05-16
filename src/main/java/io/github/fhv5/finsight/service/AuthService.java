package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.exception.ConflictException;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.repository.UserRepository;
import io.github.fhv5.finsight.security.JwtService;
import io.github.fhv5.finsight.security.SecurityUser;
import io.jsonwebtoken.Claims;
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
            throw new ConflictException("Email is already registered");
        }

        if (!request.password().equals(request.confirmPassword())) {
            // TODO: custom exception yea yea
            throw new InvalidInputException("Passwords do not match");
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

        User user = secUser.user();

        return tokenService.issueTokens(user);
    }

    public AuthDTOS.LoginResponse rotateTokens(String authHeader, String refreshToken) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidInputException("Missing or invalid Authorization header");
        }

        String jwt = authHeader.substring(7);
        Claims claims = jwtService.parseClaimsEvenIfExpired(jwt);

        if (claims.getExpiration().after(new java.util.Date())) {
            throw new InvalidInputException("Access token is not expired yet");
        }

        String jti = claims.getId();
        return tokenService.refreshTokens(jti, refreshToken);
    }
}
