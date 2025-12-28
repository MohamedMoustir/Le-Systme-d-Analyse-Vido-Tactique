package com.savt.backend.presentation.dto.Request;
import lombok.Data;

@Data
public class EquipeRequest {
    private String nom;
    private String logoUrl;
    private String couleurHex;
}