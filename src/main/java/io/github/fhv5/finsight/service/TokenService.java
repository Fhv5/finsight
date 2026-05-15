package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.config.TokenProperties;
import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.model.Token;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.repository.TokenRepository;
import io.github.fhv5.finsight.repository.UserRepository;
import io.github.fhv5.finsight.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final TokenProperties tokenProperties;
    private final UserRepository userRepository;

    public AuthDTOS.LoginResponse issueTokens(User user) {
        UUID jti = UUID.randomUUID();
        String accessToken = jwtService.generateToken(jti.toString(), user.getId().toString());
        String refreshToken = generateSecureRandomString();

        Token token = Token.builder()
                .jti(jti)
                .userId(user.getId())
                .refreshToken(refreshToken)
                .revoked(false)
                .accessTokenExpiresAt(Instant.now().plus(tokenProperties.accessTokenExpiration()))
                .refreshTokenExpiresAt(Instant.now().plus(tokenProperties.refreshTokenExpiration()))
                .build();

        tokenRepository.save(token);
        return new AuthDTOS.LoginResponse(accessToken, refreshToken);
    }

    private String generateSecureRandomString() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public AuthDTOS.LoginResponse refreshTokens(String jti, String refreshToken) {
        Token token = findActiveToken(jti);

        if (!token.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        if (token.getRefreshTokenExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        token.setRevoked(true);
        tokenRepository.save(token);

        return issueTokens(user);
    }

    public void validateAccessToken(String jti) {
        findActiveToken(jti);
    }

    private Token findActiveToken(String jti) {
        Token token = tokenRepository.findByJti(UUID.fromString(jti))
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        if (token.isRevoked()) {
            throw new IllegalArgumentException("Token revoked");
        }

        return token;
    }
}
