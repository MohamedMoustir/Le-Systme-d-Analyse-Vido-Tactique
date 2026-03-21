package com.football.analyzer.application.service.implementation;

import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.enums.Role;
import com.football.analyzer.domain.exception.DuplicateResourceException;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.infrastructure.service.AutheticatedUser;
import com.football.analyzer.infrastructure.service.JwtService;
import com.football.analyzer.infrastructure.service.RefreshTokenService;
import com.football.analyzer.presentation.dto.Request.RegisterRequest;
import com.football.analyzer.presentation.dto.Response.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldReturnLoginResponse_whenRequestIsValid() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .nom("Safiy")
                .email("safiy@test.com")
                .password("plainPassword")
                .role("COACH")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(request.getEmail(), "COACH")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(request.getEmail())).thenReturn("refresh-token");

        // When
        LoginResponse response = authService.register(request);

        // Then
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("safiy@test.com", response.getEmail());
        assertEquals("COACH", response.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("Safiy", savedUser.getNom());
        assertEquals("safiy@test.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(Role.COACH, savedUser.getRole());
        assertTrue(savedUser.isActivated());

        verify(refreshTokenService).storeRefreshtoken("refresh-token");
    }

    @Test
    void register_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .nom("Safiy")
                .email("existing@test.com")
                .password("plainPassword")
                .role("COACH")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // When / Then
        assertThrows(DuplicateResourceException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(), any());
        verify(jwtService, never()).generateRefreshToken(any());
        verify(refreshTokenService, never()).storeRefreshtoken(any());
    }

    @Test
    void register_shouldThrowIllegalArgumentException_whenRoleIsInvalid() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .nom("Safiy")
                .email("safiy@test.com")
                .password("plainPassword")
                .role("INVALID_ROLE")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(), any());
        verify(refreshTokenService, never()).storeRefreshtoken(any());
    }


}

