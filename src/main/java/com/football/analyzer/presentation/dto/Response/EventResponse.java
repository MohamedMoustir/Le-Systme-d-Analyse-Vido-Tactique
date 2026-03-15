package com.football.analyzer.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private String time;
    private Long frame;
    private String type;
    private String description;
    private String playerName;
}