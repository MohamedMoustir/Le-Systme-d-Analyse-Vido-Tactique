package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.AuthService;
import com.football.analyzer.presentation.dto.Request.LoginRequest;
import com.football.analyzer.presentation.dto.Request.RegisterRequest;
import com.football.analyzer.presentation.dto.Response.LoginResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService ;

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


}
