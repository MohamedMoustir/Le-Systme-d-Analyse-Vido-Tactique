package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.RapportService;
import com.football.analyzer.domain.entity.User;
import com.football.analyzer.domain.repository.UserRepository;
import com.football.analyzer.presentation.dto.Response.RapportGlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    private final UserRepository userRepository;

    @GetMapping("/my-stats")
    public ResponseEntity<RapportGlobalResponse> getMyStats(Principal principal) {
        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String realUserId = user.getId();

        RapportGlobalResponse response = rapportService.getMyStats(realUserId);

        return ResponseEntity.ok(response);
    }
}

