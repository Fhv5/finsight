package io.github.fhv5.finsight.security;

import io.github.fhv5.finsight.config.TokenProperties;
import io.github.fhv5.finsight.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceTest {

    @Mock
    private TokenProperties tokenProperties;

    private JwtService jwtService;

    // Base64 encoded 256-bit key for testing
    private static final String SECRET_KEY = "VGhpcyBpcyBhIHNlY3JldCBrZXkgZm9yIHRlc3RpbmcgdGhhdCBpcyBsb25nIGVub3VnaA==";

    @BeforeEach
    void setUp() {
        when(tokenProperties.secretKey()).thenReturn(SECRET_KEY);
        when(tokenProperties.accessTokenExpiration()).thenReturn(Duration.ofMinutes(15));
        jwtService = new JwtService(tokenProperties);
    }

    @Test
    void generateToken_ShouldReturnValidJwt() {
        String jti = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        String token = jwtService.generateToken(jti, userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void parseClaims_ShouldReturnValidClaims_WhenTokenIsValid() {
        String jti = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        String token = jwtService.generateToken(jti, userId);
        Claims claims = jwtService.parseClaims(token);

        assertNotNull(claims);
        assertEquals(jti, claims.getId());
        assertEquals(userId, claims.getSubject());
    }

    @Test
    void parseClaims_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        assertThrows(MalformedJwtException.class, () -> jwtService.parseClaims(invalidToken));
    }

    @Test
    void parseClaims_ShouldThrowExpiredJwtException_WhenTokenIsExpired() {
        // Set an expiration of 0 or negative
        when(tokenProperties.accessTokenExpiration()).thenReturn(Duration.ofMillis(-1000));
        JwtService expiredJwtService = new JwtService(tokenProperties);

        String jti = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        String token = expiredJwtService.generateToken(jti, userId);

        assertThrows(ExpiredJwtException.class, () -> expiredJwtService.parseClaims(token));
    }

    @Test
    void parseClaimsEvenIfExpired_ShouldReturnClaims_WhenTokenIsValid() {
        String jti = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        String token = jwtService.generateToken(jti, userId);
        Claims claims = jwtService.parseClaimsEvenIfExpired(token);

        assertNotNull(claims);
        assertEquals(jti, claims.getId());
        assertEquals(userId, claims.getSubject());
    }

    @Test
    void parseClaimsEvenIfExpired_ShouldReturnClaims_WhenTokenIsExpired() {
        when(tokenProperties.accessTokenExpiration()).thenReturn(Duration.ofMillis(-1000));
        JwtService expiredJwtService = new JwtService(tokenProperties);

        String jti = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        String token = expiredJwtService.generateToken(jti, userId);

        Claims claims = expiredJwtService.parseClaimsEvenIfExpired(token);

        assertNotNull(claims);
        assertEquals(jti, claims.getId());
        assertEquals(userId, claims.getSubject());
    }

    @Test
    void parseClaimsEvenIfExpired_ShouldThrowUnauthorizedException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        assertThrows(UnauthorizedException.class, () -> jwtService.parseClaimsEvenIfExpired(invalidToken));
    }
}
