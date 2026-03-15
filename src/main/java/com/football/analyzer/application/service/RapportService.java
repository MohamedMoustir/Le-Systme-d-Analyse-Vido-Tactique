package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.response.RapportGlobalResponse;

public interface RapportService {
    RapportGlobalResponse getMyStats(String userId);
}

