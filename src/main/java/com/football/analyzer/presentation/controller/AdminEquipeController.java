package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.implementation.AdminEquipeService;
import com.football.analyzer.presentation.dto.request.EquipeCreateDTO;
import com.football.analyzer.presentation.dto.response.EquipeAdminDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/equipes")
@AllArgsConstructor
public class AdminEquipeController {

    private final AdminEquipeService adminEquipeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EquipeAdminDTO>> getAllEquipes() {
        return ResponseEntity.ok(adminEquipeService.getAllEquipes());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipeAdminDTO> createEquipe(@RequestBody EquipeCreateDTO dto) {
        return ResponseEntity.ok(adminEquipeService.createEquipe(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteEquipe(@PathVariable String id) {
        adminEquipeService.deleteEquipe(id);
        return ResponseEntity.ok("Équipe supprimée avec succès");
    }
}