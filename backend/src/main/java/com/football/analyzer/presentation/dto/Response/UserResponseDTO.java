package com.football.analyzer.presentation.dto.Response;

import com.football.analyzer.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private String id;
    private String nom;
    private String email;
    private Role role;
    private boolean isActivated;
    private LocalDateTime creationAt;
}
