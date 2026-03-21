package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.service.AuthService;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.enums.Role;
import com.football.analyzer.domain.exception.DuplicateResourceException;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.infrastructure.service.AutheticatedUser;
import com.football.analyzer.infrastructure.service.JwtService;
import com.football.analyzer.infrastructure.service.RefreshTokenService;
import com.football.analyzer.presentation.dto.Request.RegisterRequest;
import com.football.analyzer.presentation.dto.Response.LoginResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public LoginResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User already exist with Email :" + registerRequest.getEmail());
        }
        User user = User.builder().email(registerRequest.getEmail()).password(passwordEncoder.encode(registerRequest.getPassword())).nom(registerRequest.getNom()).activated(true).role(Role.valueOf(registerRequest.getRole())).build();
        userRepository.save(user);

        String roles = String.valueOf(user.getRole());
        String accessToken = this.jwtService.generateToken(registerRequest.getEmail(), roles);
        String refreshToken = this.jwtService.generateRefreshToken(registerRequest.getEmail());

        refreshTokenService.storeRefreshtoken(refreshToken);

        return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType(BEARER_TOKEN_TYPE).email(user.getEmail()).role(roles).build();
    }

    @Override
    public LoginResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        AutheticatedUser autheticatedUser = (AutheticatedUser) authentication.getPrincipal();
        User user = autheticatedUser.getUser();

        String role = user.getRole().toString();
        String accessToken = this.jwtService.generateToken(email, role);
        String refreshToken = this.jwtService.generateRefreshToken(user.getEmail());

        refreshTokenService.storeRefreshtoken(refreshToken);

        return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType(BEARER_TOKEN_TYPE).email(user.getEmail()).role(user.getRole().toString()).build();
    }

  @Override
  public LoginResponse refreshToken(String refreshToken) {

    String userEmail = jwtService.extractUsername(refreshToken);

    if (userEmail != null) {
      User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("User not found"));

      if (jwtService.isTokenValid(refreshToken)) {

        String role = user.getRole().toString();
        String newAccessToken = this.jwtService.generateToken(userEmail, role);
        String newRefreshToken = this.jwtService.generateRefreshToken(userEmail);

        refreshTokenService.storeRefreshtoken(newRefreshToken);

        return LoginResponse.builder()
          .accessToken(newAccessToken)
          .refreshToken(newRefreshToken)
          .tokenType(BEARER_TOKEN_TYPE)
          .email(user.getEmail())
          .role(role)
          .build();
      }
    }
    throw new RuntimeException("Refresh Token invalide ou expiré");
  }
}
