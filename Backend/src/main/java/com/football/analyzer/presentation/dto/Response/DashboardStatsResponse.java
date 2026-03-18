package com.football.analyzer.presentation.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalCoaches;
    private long totalVideosAnalyzed;
    private long totalEquipes;
}