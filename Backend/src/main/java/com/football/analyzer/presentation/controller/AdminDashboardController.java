package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.AdminDashboardService;
import com.football.analyzer.presentation.dto.Response.DashboardStatsResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@AllArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getGlobalStats() {
        return ResponseEntity.ok(dashboardService.getGlobalStatistics());
    }
}