package com.football.analyzer.presentation.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoueurResponseDTO {

    private Long id;
    private String nomComplet;
    private Integer numeroMaillot;
    private String poste;
    private String photoUrl;

}