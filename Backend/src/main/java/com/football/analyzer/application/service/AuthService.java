package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.Request.RegisterRequest;
import com.football.analyzer.presentation.dto.Response.LoginResponse;

public interface AuthService {
     LoginResponse register(RegisterRequest registerRequest);
     LoginResponse login(String email, String password);
     LoginResponse refreshToken(String refreshToken);
}
