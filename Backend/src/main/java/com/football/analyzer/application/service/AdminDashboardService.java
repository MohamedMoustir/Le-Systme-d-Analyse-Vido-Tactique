package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.Response.DashboardStatsResponse;


public interface AdminDashboardService {
    DashboardStatsResponse getGlobalStatistics();
}
