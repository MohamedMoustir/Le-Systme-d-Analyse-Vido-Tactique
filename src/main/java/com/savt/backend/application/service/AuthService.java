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

       return generateLoginResponse(user);
    }

    public LoginResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        AutheticatedUser autheticatedUser = (AutheticatedUser) authentication.getPrincipal();
        User user = autheticatedUser.getUser();

        return generateLoginResponse(user);

    }

    public LoginResponse loginFromSocial(User user) {
        return generateLoginResponse(user);
    }

    public LoginResponse refreshToken(String incomingRefreshToken) {

        boolean isValid = refreshTokenService.validateRefreshToken(incomingRefreshToken);
        if (!isValid) {
            throw new RuntimeException("Refresh Token Invalide ou Expiré (Veuillez vous reconnecter)");
        }

        String email = refreshTokenService.getUsernameFromRefreshToken(incomingRefreshToken);

        if (email == null) {
            throw new RuntimeException("Session introuvable");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole().toString());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(incomingRefreshToken)
                .tokenType(BEARER_TOKEN_TYPE)
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build();
    }

    private LoginResponse generateLoginResponse(User user) {
        String role = user.getRole().toString();
        String accessToken = this.jwtService.generateToken(user.getEmail(), role);
        String refreshToken = this.jwtService.generateRefreshToken(user.getEmail());

        refreshTokenService.storeRefreshtoken(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(BEARER_TOKEN_TYPE)
                .email(user.getEmail())
                .role(role)
                .build();
    }
}
