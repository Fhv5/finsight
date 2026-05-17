package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.config.TokenProperties;
import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.exception.UnauthorizedException;
import io.github.fhv5.finsight.model.Token;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.repository.TokenRepository;
import io.github.fhv5.finsight.repository.UserRepository;
import io.github.fhv5.finsight.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenProperties tokenProperties;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenService tokenService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@test.com");
    }

    @Test
    void issueTokens_ShouldSaveTokenAndReturnLoginResponse() {
        when(tokenProperties.accessTokenExpiration()).thenReturn(Duration.ofMinutes(15));
        when(tokenProperties.refreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("access.token.mock");
        
        AuthDTOS.LoginResponse response = tokenService.issueTokens(mockUser);

        assertNotNull(response);
        assertEquals("access.token.mock", response.accessToken());
        assertNotNull(response.refreshToken());

        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        Token savedToken = tokenCaptor.getValue();
        assertEquals(mockUser.getId(), savedToken.getUserId());
        assertFalse(savedToken.isRevoked());
        assertNotNull(savedToken.getRefreshToken());
    }

    @Test
    void refreshTokens_ShouldReturnNewTokens_WhenValid() {
        UUID jti = UUID.randomUUID();
        String oldRefreshToken = "oldRefreshToken";

        Token oldToken = new Token();
        oldToken.setJti(jti);
        oldToken.setUserId(mockUser.getId());
        oldToken.setRefreshToken(oldRefreshToken);
        oldToken.setRefreshTokenExpiresAt(Instant.now().plus(Duration.ofDays(1)));
        oldToken.setRevoked(false);

        when(tokenRepository.findByJti(jti)).thenReturn(Optional.of(oldToken));
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        
        when(tokenProperties.accessTokenExpiration()).thenReturn(Duration.ofMinutes(15));
        when(tokenProperties.refreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("new.access.token");

        AuthDTOS.LoginResponse response = tokenService.refreshTokens(jti.toString(), oldRefreshToken);

        assertNotNull(response);
        assertEquals("new.access.token", response.accessToken());
        
        assertTrue(oldToken.isRevoked());
        verify(tokenRepository, times(2)).save(any(Token.class)); // 1 for revoking old, 1 for issuing new
    }

    @Test
    void refreshTokens_ShouldThrowException_WhenTokenRevoked() {
        UUID jti = UUID.randomUUID();
        Token revokedToken = new Token();
        revokedToken.setRevoked(true);

        when(tokenRepository.findByJti(jti)).thenReturn(Optional.of(revokedToken));

        assertThrows(UnauthorizedException.class, () -> tokenService.refreshTokens(jti.toString(), "refresh"));
    }

    @Test
    void refreshTokens_ShouldThrowException_WhenRefreshTokenInvalid() {
        UUID jti = UUID.randomUUID();
        Token token = new Token();
        token.setRevoked(false);
        token.setRefreshToken("validRefresh");

        when(tokenRepository.findByJti(jti)).thenReturn(Optional.of(token));

        assertThrows(UnauthorizedException.class, () -> tokenService.refreshTokens(jti.toString(), "invalidRefresh"));
    }

    @Test
    void refreshTokens_ShouldThrowException_WhenRefreshTokenExpired() {
        UUID jti = UUID.randomUUID();
        Token token = new Token();
        token.setRevoked(false);
        token.setRefreshToken("validRefresh");
        token.setRefreshTokenExpiresAt(Instant.now().minus(Duration.ofDays(1)));

        when(tokenRepository.findByJti(jti)).thenReturn(Optional.of(token));

        assertThrows(UnauthorizedException.class, () -> tokenService.refreshTokens(jti.toString(), "validRefresh"));
    }

    @Test
    void validateAccessToken_ShouldNotThrow_WhenTokenValid() {
        UUID jti = UUID.randomUUID();
        Token token = new Token();
        token.setRevoked(false);

        when(tokenRepository.findByJti(jti)).thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> tokenService.validateAccessToken(jti.toString()));
    }

    @Test
    void validateAccessToken_ShouldThrow_WhenTokenRevoked() {
        UUID jti = UUID.randomUUID();
        Token token = new Token();
        token.setRevoked(true);

        when(tokenRepository.findByJti(jti)).thenReturn(Optional.of(token));

        assertThrows(UnauthorizedException.class, () -> tokenService.validateAccessToken(jti.toString()));
    }

    @Test
    void validateAccessToken_ShouldThrow_WhenTokenNotFound() {
        UUID jti = UUID.randomUUID();
        when(tokenRepository.findByJti(jti)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tokenService.validateAccessToken(jti.toString()));
    }
}
