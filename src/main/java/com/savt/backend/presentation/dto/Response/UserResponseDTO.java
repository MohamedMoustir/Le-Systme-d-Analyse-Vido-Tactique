package com.savt.backend.presentation.dto.Response;

import com.savt.backend.domain.enums.Role;
import com.savt.backend.domain.enums.Social;
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
    private Social provider;
    private boolean isActivated;
    private LocalDateTime creationAt;
}
