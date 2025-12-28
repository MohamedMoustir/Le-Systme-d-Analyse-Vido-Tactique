package com.savt.backend.presentation.controller;

import com.savt.backend.application.service.TeamManagementService;
import com.savt.backend.domain.entity.Equipe;
import com.savt.backend.domain.entity.Joueur;
import com.savt.backend.presentation.dto.Request.EquipeRequest;
import com.savt.backend.presentation.dto.Request.JoueurRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamManagementService teamService;

    @PostMapping
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<Equipe> createEquipe(@RequestBody EquipeRequest request) {
        return ResponseEntity.ok(teamService.createEquipe(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN' , 'COACH')")
    public ResponseEntity<List<Equipe>> getAllEquipes() {
        return ResponseEntity.ok(teamService.getAllEquipes());
    }

    @PostMapping("/joueurs")
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<Joueur> addJoueur(@RequestBody JoueurRequest request) {
        return ResponseEntity.ok(teamService.addJoueurToTeam(request));
    }

    @GetMapping("/{equipeId}/joueurs")
    @PreAuthorize("hasAnyRole('ADMIN' , 'COACH')")
    public ResponseEntity<List<Joueur>> getJoueursByTeam(@PathVariable String equipeId) {
        return ResponseEntity.ok(teamService.getJoueursByEquipe(equipeId));
    }
}