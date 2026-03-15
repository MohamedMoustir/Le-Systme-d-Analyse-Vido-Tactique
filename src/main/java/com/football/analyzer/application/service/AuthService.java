package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.request.RegisterRequest;
import com.football.analyzer.presentation.dto.response.LoginResponse;

public interface AuthService {
     LoginResponse register(RegisterRequest registerRequest);
     LoginResponse login(String email, String password);
}
