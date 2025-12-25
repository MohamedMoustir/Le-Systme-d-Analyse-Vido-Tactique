package com.savt.backend.application.service;

import com.savt.backend.domain.entity.User;
import com.savt.backend.domain.enums.Role;
import com.savt.backend.domain.exception.DuplicateResourceException;
import com.savt.backend.domain.repository.UserRepository;
import com.savt.backend.infrastructure.service.AutheticatedUser;
import com.savt.backend.infrastructure.service.JwtService;
import com.savt.backend.infrastructure.service.RefreshTokenService;
import com.savt.backend.presentation.dto.Request.RegisterRequest;
import com.savt.backend.presentation.dto.Response.LoginResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private final RedisTemplate<String ,Object> redisTemplate ;


    public LoginResponse register(RegisterRequest registerRequest){
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()){
            throw new DuplicateResourceException("User already exist with Email :" + registerRequest.getEmail());
        }
        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nom(registerRequest.getNom())
                .isActivated(true)
                .role(Role.valueOf(registerRequest.getRole()))
                .build();
        userRepository.save(user);

        String roles = user.getRole().toString();
        String accessToken = this.jwtService.generateToken(registerRequest.getEmail(), roles);
        String refreshToken = this.jwtService.generateRefreshToken(registerRequest.getEmail());

        refreshTokenService.storeRefreshtoken(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(BEARER_TOKEN_TYPE)
                .email(user.getEmail())
                .role(roles)
                .build();
    }

    public LoginResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        AutheticatedUser autheticatedUser = (AutheticatedUser) authentication.getPrincipal();
        User user = autheticatedUser.getUser();

        String role = user.getRole().toString();
        String accessToken = this.jwtService.generateToken(email, role);
        String refreshToken = this.jwtService.generateRefreshToken(user.getEmail());

        refreshTokenService.storeRefreshtoken(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(BEARER_TOKEN_TYPE)
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build();
    }
}
