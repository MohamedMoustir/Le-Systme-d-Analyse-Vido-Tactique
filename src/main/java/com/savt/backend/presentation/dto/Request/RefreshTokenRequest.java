package com.savt.backend.presentation.dto.Request;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String token;
}