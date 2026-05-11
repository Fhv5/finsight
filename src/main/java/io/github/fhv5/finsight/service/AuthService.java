package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthDTOS.LoginResponse register(AuthDTOS.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            // TODO: Change to custom exception and handle it in a global exception handler
            throw new IllegalArgumentException("Email already in use");
        }

        if (!request.password().equals(request.confirmPassword())) {
            // TODO: custom exception yea yea
            throw new IllegalArgumentException("Passwords do not match");
        }

        String encryptedPassword = passwordEncoder.encode(request.password()); // Todo: Encrypt password

        User newUser = User.builder()
                .email(request.email())
                .password(encryptedPassword)
                .build();

        User savedUser = userRepository.save(newUser);

        return new AuthDTOS.LoginResponse("change this bro");
    }

    public AuthDTOS.LoginResponse login(AuthDTOS.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return new AuthDTOS.LoginResponse("change this bro");
    }

    public AuthDTOS.LoginResponse refreshToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        return new AuthDTOS.LoginResponse("change this bro");
    }
}
