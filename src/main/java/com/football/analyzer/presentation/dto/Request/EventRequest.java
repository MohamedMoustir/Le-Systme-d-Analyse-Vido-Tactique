package com.football.analyzer.presentation.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    private String videoId;
    private Long frameNumber;
    private String type;
    private String description;
    private String joueurId;
}