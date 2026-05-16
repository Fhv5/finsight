package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.AuthDTOS;
import io.github.fhv5.finsight.exception.ConflictException;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.repository.UserRepository;
import io.github.fhv5.finsight.security.JwtService;
import io.github.fhv5.finsight.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("encodedPassword");
    }

    @Test
    void register_ShouldSaveUserAndReturnTokens_WhenValid() {
        AuthDTOS.RegisterRequest request = new AuthDTOS.RegisterRequest("test@test.com", "password", "password");
        
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        
        AuthDTOS.LoginResponse expectedResponse = new AuthDTOS.LoginResponse("accessToken", "refreshToken");
        when(tokenService.issueTokens(mockUser)).thenReturn(expectedResponse);

        AuthDTOS.LoginResponse actualResponse = authService.register(request);

        assertEquals(expectedResponse, actualResponse);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailInUse() {
        AuthDTOS.RegisterRequest request = new AuthDTOS.RegisterRequest("test@test.com", "password", "password");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void register_ShouldThrowException_WhenPasswordsDoNotMatch() {
        AuthDTOS.RegisterRequest request = new AuthDTOS.RegisterRequest("test@test.com", "password", "differentPassword");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        assertThrows(InvalidInputException.class, () -> authService.register(request));
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsAreValid() {
        AuthDTOS.LoginRequest request = new AuthDTOS.LoginRequest("test@test.com", "password");
        
        Authentication authentication = mock(Authentication.class);
        SecurityUser secUser = new SecurityUser(mockUser);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(secUser);
        
        AuthDTOS.LoginResponse expectedResponse = new AuthDTOS.LoginResponse("accessToken", "refreshToken");
        when(tokenService.issueTokens(mockUser)).thenReturn(expectedResponse);

        AuthDTOS.LoginResponse actualResponse = authService.login(request);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void rotateTokens_ShouldReturnNewTokens_WhenAccessTokenExpired() {
        String jti = UUID.randomUUID().toString();
        Claims claims = Jwts.claims().id(jti).expiration(new java.util.Date(System.currentTimeMillis() - 1000)).build();
        String jwt = Jwts.builder().claims(claims).compact();
        String refreshToken = "refreshToken";
        String authHeader = "Bearer " + jwt;

        when(jwtService.parseClaimsEvenIfExpired(jwt)).thenReturn(claims);

        AuthDTOS.LoginResponse expectedResponse = new AuthDTOS.LoginResponse("newAccessToken", "newRefreshToken");
        when(tokenService.refreshTokens(jti, refreshToken)).thenReturn(expectedResponse);

        AuthDTOS.LoginResponse actualResponse = authService.rotateTokens(authHeader, refreshToken);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void rotateTokens_ShouldThrowException_WhenAccessTokenNotExpired() {
        String authHeader = "Bearer validAccessToken";
        String refreshToken = "refreshToken";

        Claims claims = Jwts.claims().id(UUID.randomUUID().toString()).expiration(new java.util.Date(System.currentTimeMillis() + 10000)).build();
        when(jwtService.parseClaimsEvenIfExpired("validAccessToken")).thenReturn(claims);

        assertThrows(InvalidInputException.class, () -> authService.rotateTokens(authHeader, refreshToken));
    }
}
