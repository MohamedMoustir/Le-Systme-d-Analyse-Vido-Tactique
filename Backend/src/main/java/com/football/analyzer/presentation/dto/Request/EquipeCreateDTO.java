package com.football.analyzer.presentation.dto.Request;

import lombok.Data;

@Data
public class EquipeCreateDTO {
    private String nom;
    private String logoUrl;
    private String couleurPrimaire;
}