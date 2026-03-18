package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.Response.RapportGlobalResponse;

public interface RapportService {
    RapportGlobalResponse getMyStats(String userId);
}

