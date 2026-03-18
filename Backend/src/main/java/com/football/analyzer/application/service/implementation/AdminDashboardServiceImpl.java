package com.football.analyzer.application.service.implementation;

import com.football.analyzer.application.service.AdminDashboardService;
import com.football.analyzer.domain.enums.Role;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.domain.repository.VideoRepository;
import com.football.analyzer.domain.repository.EquipeRepository;
import com.football.analyzer.presentation.dto.Response.DashboardStatsResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final EquipeRepository equipeRepository;

    @Override
    public DashboardStatsResponse getGlobalStatistics() {
        long totalUsers = userRepository.count();

        long totalCoaches = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.COACH)
                .count();

        long totalVideos = videoRepository.count();

        long totalEquipes = equipeRepository.count();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalCoaches(totalCoaches)
                .totalVideosAnalyzed(totalVideos)
                .totalEquipes(totalEquipes)
                .build();
    }
}