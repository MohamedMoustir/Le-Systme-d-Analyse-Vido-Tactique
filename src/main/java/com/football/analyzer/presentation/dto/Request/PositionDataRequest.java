package com.football.analyzer.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PositionDataRequest {
    private Long frameNumber;
    private Float x;
    private Float y;
    private Float speed;
    private String joueurId;

}