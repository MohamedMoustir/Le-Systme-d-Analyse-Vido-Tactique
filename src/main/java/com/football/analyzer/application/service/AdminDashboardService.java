package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.response.DashboardStatsResponse;

public interface AdminDashboardService {
    DashboardStatsResponse getGlobalStatistics();
}