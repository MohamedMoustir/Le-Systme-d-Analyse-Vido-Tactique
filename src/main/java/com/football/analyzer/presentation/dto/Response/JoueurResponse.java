package com.football.analyzer.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoueurResponse {
    private String id;
    private String nomComplet;
    private Integer numeroMaillot;
    private String poste;
    private String photoUrl;
}