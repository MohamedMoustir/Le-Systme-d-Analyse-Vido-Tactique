package com.football.analyzer.presentation.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReglageDTO {
    private String nomComplet;
    private String email;
    private String nomClub;
    private String couleurClub;
    private boolean notificationsActives;
    private String langue;
}