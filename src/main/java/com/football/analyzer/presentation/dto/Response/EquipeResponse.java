package com.football.analyzer.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipeResponse {
    private String id;
    private String nom;
    private String logoUrl;
    private String couleurHex;
    private List<JoueurResponse> joueurs;
}