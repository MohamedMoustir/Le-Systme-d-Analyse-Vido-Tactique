package com.football.analyzer.presentation.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipeAdminDTO {
    private String id;
    private String nom;
    private String logoUrl;
    private String couleurPrimaire;
}