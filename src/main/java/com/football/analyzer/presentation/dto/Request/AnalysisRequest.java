package com.football.analyzer.presentation.dto.Request;

import lombok.Data;

@Data
public class AnalysisRequest {

    private String videoPath;
    private String device = "cpu";
}