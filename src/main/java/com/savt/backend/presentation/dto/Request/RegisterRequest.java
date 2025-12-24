package com.savt.backend.presentation.dto.Request;

import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Email(message = "Email invalide")
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String role;
}