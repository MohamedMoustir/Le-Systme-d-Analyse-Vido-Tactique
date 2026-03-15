package com.football.analyzer.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoUploadRequest {
    private String titre;
    private String homeTeamId;
    private String awayTeamId;
}


