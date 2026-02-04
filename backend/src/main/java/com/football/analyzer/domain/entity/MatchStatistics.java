package com.football.analyzer.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchStatistics {
    private Float possessionHome;
    private Float possessionAway;
    private Float distanceTotaleHome;
    private Float distanceTotaleAway;
}
