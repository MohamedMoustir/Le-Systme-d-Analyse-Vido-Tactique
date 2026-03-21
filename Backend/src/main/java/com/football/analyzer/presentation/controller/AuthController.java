package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.AuthService;
import com.football.analyzer.application.service.UserService;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.presentation.dto.Request.LoginRequest;
import com.football.analyzer.presentation.dto.Request.RefreshTokenRequest;
import com.football.analyzer.presentation.dto.Request.RegisterRequest;
import com.football.analyzer.presentation.dto.Response.LoginResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.security.Principal;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService ;
    private final UserService userServise;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest registerRequest){
         LoginResponse response =   authService.register(registerRequest);
         return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse response = authService.login(loginRequest.getEmail() ,loginRequest.getPassword());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile(Principal principal) {

        String email = principal.getName();

        Optional<User> userProfile = userServise.getUserByEmail(email);

        return ResponseEntity.ok(userProfile);
    }
  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
    LoginResponse response = authService.refreshToken(request.getRefreshToken());
    return ResponseEntity.ok(response);
  }

}
