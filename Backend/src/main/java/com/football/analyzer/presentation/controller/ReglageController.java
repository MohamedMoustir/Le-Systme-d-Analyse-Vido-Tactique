package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.ReglageService;
import com.football.analyzer.presentation.dto.Response.ReglageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/reglages")
@RequiredArgsConstructor
public class ReglageController {

    private final ReglageService reglageService;

    @GetMapping
    public ResponseEntity<ReglageDTO> getMesReglages(Principal principal) {
        String userId = principal.getName();
        return ResponseEntity.ok(reglageService.getReglages(userId));
    }

    @PutMapping
    public ResponseEntity<ReglageDTO> updateMesReglages(Principal principal, @RequestBody ReglageDTO dto) {
        String userId = principal.getName();
        return ResponseEntity.ok(reglageService.updateReglages(userId, dto));
    }


}