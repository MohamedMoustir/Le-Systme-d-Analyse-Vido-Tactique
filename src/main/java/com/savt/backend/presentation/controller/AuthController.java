package com.savt.backend.presentation.controller;

import com.savt.backend.application.service.AuthService;
import com.savt.backend.application.service.UserServise;
import com.savt.backend.domain.entity.User;
import com.savt.backend.presentation.dto.Request.LoginRequest;
import com.savt.backend.presentation.dto.Request.RefreshTokenRequest;
import com.savt.backend.presentation.dto.Request.RegisterRequest;
import com.savt.backend.presentation.dto.Response.LoginResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService ;
    private final UserServise userService;

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

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getToken());
        return ResponseEntity.ok(response);
    }


}
