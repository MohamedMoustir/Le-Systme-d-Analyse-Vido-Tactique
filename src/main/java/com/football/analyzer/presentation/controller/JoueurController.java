package com.football.analyzer.presentation.controller;

import com.football.analyzer.application.service.JoueurService;
import com.football.analyzer.domain.entity.Joueur;
import com.football.analyzer.presentation.dto.response.JoueurResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/equipes/joueur")
@RequiredArgsConstructor
public class JoueurController {
    @GetMapping("/{id}")
    public ResponseEntity<JoueurResponseDTO> getJoueur(@PathVariable String id) {
        return ResponseEntity.ok(joueurService.getJoueurById(id));
    }
    private final JoueurService joueurService;


    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateJoueur(
            @PathVariable String id,
            @RequestParam("nomComplet") String nomComplet,
            @RequestParam("numeroMaillot") Integer numeroMaillot,
            @RequestParam("poste") String poste,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        try {
            return ResponseEntity.ok(joueurService.updateJoueur(id, nomComplet, numeroMaillot, poste, photo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJoueur(@PathVariable String id) {
        joueurService.deleteJoueur(id);
        return ResponseEntity.noContent().build();
    }
}