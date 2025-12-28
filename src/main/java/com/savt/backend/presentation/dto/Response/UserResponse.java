package com.savt.backend.presentation.dto.Response;

import com.savt.backend.domain.enums.Social;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String id;
    private String nom;
    private String email;
    private String role;
    private Social provider;
    private String token;
}