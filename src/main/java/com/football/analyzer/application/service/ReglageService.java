package com.football.analyzer.application.service;

import com.football.analyzer.presentation.dto.response.ReglageDTO;

public interface ReglageService {
    ReglageDTO getReglages(String userId);
    ReglageDTO updateReglages(String userId, ReglageDTO dto);
}