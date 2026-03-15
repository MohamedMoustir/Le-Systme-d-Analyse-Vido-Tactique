package com.football.analyzer.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing tokens and user information")
public class LoginResponse {
    @Schema(description = "JWT access token (valid for 15 minutes)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh token for obtaining new access tokens (valid for 7 days)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Schema(description = "User role")
    private String role;
}
